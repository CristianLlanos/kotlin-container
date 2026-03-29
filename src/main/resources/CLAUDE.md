# Kotlin Container

Lightweight dependency injection container for Kotlin.

**Maven coordinates:** `com.cristianllanos:container:0.3.1`

## Core concepts

- **Auto-resolution**: Concrete classes are resolved automatically via reflection on their primary constructor. No registration needed.
- **Bindings**: `factory` (new instance each time), `singleton` (one global instance), `scoped` (one instance per scope).
- **Scopes**: Child containers with lifecycle management. Scoped instances are created once per scope and cleaned up on close.
- **Service providers**: Classes with a `register()` method that group related bindings together.
- **Callable invocation**: `call()` invokes any Kotlin function with parameters resolved from the container.
- **Thread safety**: Resolution is safe from multiple threads concurrently. Register during setup, resolve from anywhere.

## Usage patterns

```kotlin
// Create a container
val container = Container()

// Auto-resolve a concrete class
val service = container.resolve<UserService>()

// Register bindings
container.factory<PaymentGateway> { StripeGateway() }
container.singleton<Logger> { ConsoleLogger() }
container.scoped<DbConnection> { DbConnection() }
    .onClose { it.disconnect() }

// Parameterless registration (auto-resolves constructor)
container.singleton<TenantService>()

// Service providers
container.register(AuthServiceProvider())

// Scopes
container.scope { scope ->
    val db = scope.resolve<DbConnection>()
}

// Call functions with DI
container.call(::sendWelcomeEmail)

// DSL builder
val container2 = Container {
    singleton<Logger> { ConsoleLogger() }
}

// Convenience extensions
container.resolveOrNull<Logger>()          // null if unresolvable
container.has<Logger>()                    // true if resolvable
val logger by container.lazy<Logger>()     // deferred resolution
```

## API reference

| Type | Role |
|------|------|
| `Container` | Main interface combining `Registrar`, `Resolver`, and `Caller` |
| `Registrar` | Registers `factory`, `singleton`, and `scoped` bindings |
| `Resolver` | Resolves instances by type |
| `Caller` | Invokes callables with auto-resolved parameters |
| `Scope` | Child container with lifecycle; extends `Container` and `AutoCloseable` |
| `AutoResolver` | Strategy for resolving unregistered types |
| `ReflectionAutoResolver` | Default `AutoResolver` using Kotlin reflection |
| `ScopedRegistration` | Returned by `scoped()` to attach `onClose` hooks |
| `Container()` | Factory function to create a new container |
| `Container() { }` | DSL builder — creates and configures in one call |
| `Container.scope {}` | Opens a child scope with automatic cleanup |
| `Container.scope(providers) {}` | Opens a scope with providers pre-registered |
| `Resolver.resolveOrNull()` | Returns `null` instead of throwing when unresolvable |
| `Resolver.has()` | Returns `true` if a type is resolvable |
| `Resolver.lazy()` | Returns a `Lazy<T>` that resolves on first access |
