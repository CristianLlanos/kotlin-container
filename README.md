# kotlin-container

A lightweight dependency injection container for Kotlin. Zero config, type-safe, reflection-based auto-resolution.

## Installation

Add the GitHub Packages repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/CristianLlanos/kotlin-container")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.cristianllanos:container:0.1.0")
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

## Service providers

Group related registrations into providers for modularity:

```kotlin
class AuthServiceProvider : ServiceProvider {
    override fun register(container: Container) {
        container.singleton<TokenStore> { RedisTokenStore() }
    }
}

val container = Container()
container.register(AuthServiceProvider())

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

## Interface segregation

The container is split into focused interfaces. Use them to restrict what each part of your code can do:

```kotlin
interface Registrar   // register(), factory(), singleton()
interface Resolver    // resolve()
interface Caller      // call()
interface Container : Registrar, Resolver, Caller
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
