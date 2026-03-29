# Module container

A lightweight dependency injection container for Kotlin. Constructor auto-resolution, scoped lifecycles, service providers, and thread safety — no code generation, no annotations, zero configuration.

# Package com.cristianllanos.container

Core dependency injection container providing registration, resolution, scoping, and callable invocation.

## Overview

The container auto-resolves concrete classes via reflection — no registration needed.
For interfaces and abstract types, use `factory`, `singleton`, or `scoped` bindings.

## Quick start

```kotlin
val container = Container()

// Concrete classes resolve automatically
val service = container.resolve<UserService>()

// Bind interfaces explicitly
container.singleton<Logger> { ConsoleLogger() }

// Scoped bindings for lifecycle management
container.scoped<DbConnection> { DbConnection() }
    .onClose { it.disconnect() }

container.scope { scope ->
    val db = scope.resolve<DbConnection>()
}
```
