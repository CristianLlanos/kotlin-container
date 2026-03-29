# kotlin-container

A lightweight dependency injection container for Kotlin. Zero config, type-safe, reflection-based auto-resolution.

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.cristianllanos:container:0.4.0")
}
```

## Quick start

Concrete classes are resolved automatically — no registration needed:

```kotlin
class Logger
class UserRepository(val logger: Logger)
class UserService(val repo: UserRepository)

val container = Container()
val service = container.resolve<UserService>() // resolves the entire dependency tree
```

Or use the DSL builder for setup-in-one-shot:

```kotlin
val container = Container {
    singleton<NotificationService> { SlackNotificationService() }
    factory<PaymentGateway> { StripeGateway() }
}
```

The container inspects primary constructors and recursively resolves each parameter.

Auto-resolution handles:

- **Concrete classes** — resolved recursively via their primary constructor
- **Registered interfaces/abstracts** — resolved from the registry
- **Optional parameters with defaults** — skipped when unresolvable
- **Required primitives (String, Int, etc.)** — throws `UnresolvableDependencyException`

## Manual registration

Use `factory` or `singleton` when you need explicit control — typically for binding interfaces to implementations:

```kotlin
val container = Container()

// Bind an interface to a concrete class (new instance every time)
container.factory<PaymentGateway> { StripeGateway() }

// Bind as a shared instance (created once, reused)
container.singleton<NotificationService> { SlackNotificationService() }
```

For concrete classes that just need auto-resolution with a specific lifetime, skip the lambda entirely:

```kotlin
container.singleton<TenantService>()
container.singleton<CalendarService>()
container.scoped<RequestContext>()
container.factory<TempProcessor>()
```

This is equivalent to `container.singleton<TenantService> { resolve() }` — the container auto-resolves the constructor dependencies.

Inside registration lambdas, `resolve<T>()` is available to reference other bindings — useful when one binding depends on another or when the same implementation backs multiple interfaces:

```kotlin
class EventServiceProvider {
    fun register(container: Container) {
        container.singleton<EventBus> { EventBus(this) }
        container.singleton<Emitter> { resolve<EventBus>() }
        container.singleton<Subscriber> { resolve<EventBus>() }
    }
}
```

`this` refers to the container itself, so you can pass it directly to classes that need it. `resolve<T>()` pulls from the container's registry, letting you wire shared instances across multiple interface bindings.

## Service providers

Group related registrations into providers. Any class with a `register()` method works — its parameters are auto-resolved from the container:

```kotlin
class AuthServiceProvider {
    fun register(container: Container) {
        container.singleton<TokenStore> { RedisTokenStore() }
    }
}

val container = Container()
container.register(AuthServiceProvider())
```

Providers can ask for any dependency, not just the container:

```kotlin
class OrderEventProvider {
    fun register(subscriber: Subscriber) {
        subscriber.subscribe<OrderPlaced>(
            InventoryListener::class,
            NotificationListener::class,
        )
    }
}
```

## Calling functions

Invoke any function with its dependencies resolved from the container:

```kotlin
fun sendWelcomeEmail(userService: UserService, emailService: EmailService): Boolean {
    // ...
}

val result = container.call(::sendWelcomeEmail)
```

Works with instance methods too:

```kotlin
val controller = OrderController()
container.call(controller::processOrder)
```

## Scopes

Scoped bindings create one instance per scope — a singleton within a lifecycle boundary. Useful when a dependency must be shared within a context (like an HTTP request) but isolated between contexts.

Three lifetimes:

| Lifetime | Behavior |
|---|---|
| `factory` | New instance every `resolve()` call |
| `singleton` | One instance forever (global) |
| `scoped` | One instance per scope |

Register scoped bindings on the container:

```kotlin
val container = Container()
container.scoped<DbConnection> { DbConnection(resolve<Config>()) }
```

Scoped bindings can only be resolved within a scope — resolving from the root throws `ScopeRequiredException`:

```kotlin
container.resolve<DbConnection>() // throws ScopeRequiredException

val scope = container.child()
scope.resolve<DbConnection>()     // works — creates instance
scope.resolve<DbConnection>()     // same instance
scope.close()                      // instance disposed
```

Use the block-based syntax for automatic cleanup:

```kotlin
container.scope { scope ->
    val db = scope.resolve<DbConnection>()
    // use db...
}  // scope auto-closes here
```

### Dispose hooks

Attach `onClose` to run cleanup when the scope closes:

```kotlin
container.scoped<DbConnection> { DbConnection() }
    .onClose { it.disconnect() }
```

Instances implementing `AutoCloseable` are closed automatically — no hook needed:

```kotlin
container.scoped<InputStream> { FileInputStream("data.bin") }
// .close() called automatically when scope closes
```

If both `onClose` and `AutoCloseable` apply, only the explicit `onClose` runs.

### Nested scopes

Scopes can be nested. Each scope gets its own scoped instances, and closing a parent cascades to children (deepest first):

```kotlin
container.scope { outer ->
    val outerDb = outer.resolve<DbConnection>()

    outer.scope { inner ->
        val innerDb = inner.resolve<DbConnection>()  // different instance
        // inner closes first
    }
    // outer closes after
}
```

### Scopes as child containers

A scope is a full `Container` — you can register ad-hoc bindings on it:

```kotlin
container.scope { scope ->
    scope.singleton<RequestId> { RequestId.generate() }
    scope.resolve<RequestHandler>()  // can depend on RequestId
}
```

### Contextual scopes with service providers

Use service providers to set up different scope contexts. The scope's purpose is defined by what you register on it — no named scopes needed:

```kotlin
class RequestScopeProvider(private val request: HttpRequest) {
    fun register(container: Container) {
        container.singleton<RequestId> { RequestId(request.id) }
        container.singleton<CurrentUser> { CurrentUser(request.userId) }
        container.scoped<DbTransaction> { DbTransaction(resolve<DataSource>()) }
            .onClose { it.rollbackIfOpen() }
    }
}

class JobScopeProvider(private val job: Job) {
    fun register(container: Container) {
        container.singleton<JobContext> { JobContext(job) }
    }
}
```

Then create the right scope for each context:

```kotlin
// HTTP request
fun handleRequest(container: Container, request: HttpRequest) {
    container.scope { scope ->
        scope.register(RequestScopeProvider(request))
        scope.resolve<RequestHandler>().handle()
    }
}

// Background job
fun processJob(container: Container, job: Job) {
    container.scope { scope ->
        scope.register(JobScopeProvider(job))
        scope.resolve<JobProcessor>().run()
    }
}
```

### Android

In Android, the system controls Activity and Fragment lifecycles — you can't wrap them in a `scope { }` block. Instead, tie scopes to lifecycle callbacks:

```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var scope: Scope

    override fun onCreate(savedInstanceState: Bundle?) {
        scope = appContainer.child()
        scope.register(ActivityScopeProvider(this))

        val presenter = scope.resolve<Presenter>()
    }

    override fun onDestroy() {
        scope.close()
        super.onDestroy()
    }
}
```

A dedicated Android integration package with automatic lifecycle management is planned for the future.

## Thread safety

The container is safe for concurrent resolution from multiple threads. Guarantees:

- **Singletons** are created exactly once. If multiple threads resolve the same singleton concurrently, one thread executes the factory while the others wait.
- **Scoped instances** are created once per scope, even under contention.
- **Factories** execute independently on each thread with no shared mutable state.
- **Circular dependency detection** is per-thread and does not produce false positives under concurrency.

Registration (`factory`, `singleton`, `scoped`, `register`) should happen during a single-threaded setup phase before concurrent resolution begins. The typical pattern is: bootstrap the container on startup, then share it across threads for resolution only.

## Custom auto-resolver

Replace the default reflection-based auto-resolution with your own strategy:

```kotlin
class MyAutoResolver : AutoResolver {
    override fun <T : Any> resolve(type: Class<T>, resolver: Resolver): T {
        // your resolution logic
    }
}

val container = Container(MyAutoResolver())
```

The default `ReflectionAutoResolver` is used when no custom resolver is provided.

## Convenience extensions

### Optional resolution

```kotlin
val logger = container.resolveOrNull<Logger>()   // returns null if unresolvable
val hasLogger = container.has<Logger>()           // true if resolvable
```

Note: both `resolveOrNull` and `has` trigger a full resolution — singleton and factory instances will be created as a side effect on success.

### Lazy resolution

Defer resolution until first access:

```kotlin
val logger by container.lazy<Logger>()  // resolves on first use
```

When used with a `Scope`, do not store the `Lazy` beyond that scope's lifetime.

### Scopes with providers

Register providers and execute in one call:

```kotlin
container.scope(RequestScopeProvider(request)) { scope ->
    scope.resolve<RequestHandler>().handle()
}
```

## Interface segregation

The container is split into focused interfaces. Use them to restrict what each part of your code can do:

```kotlin
interface Registrar   // register(), factory(), singleton(), scoped()
interface Resolver    // resolve()
interface Caller      // call()
interface Container : Registrar, Resolver, Caller  // child()
interface Scope : Container, AutoCloseable          // close()
```

For example, give application code only what it needs:

```kotlin
// Setup — full access
fun bootstrap(): Container {
    val container = Container()
    container.register(AuthServiceProvider(), PaymentServiceProvider())
    return container
}

// Routes — can only resolve, not register
fun userRoutes(resolver: Resolver) {
    val service = resolver.resolve<UserService>()
}

// Middleware — can only call functions
fun runMiddleware(caller: Caller) {
    caller.call(::authenticate)
}
```

## License

MIT
