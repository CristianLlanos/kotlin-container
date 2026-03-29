# kotlin-container

A lightweight dependency injection container for Kotlin. Constructor auto-resolution, scoped lifecycles, service providers, and thread safety — no code generation, no annotations, zero configuration.

[![Maven Central](https://img.shields.io/maven-central/v/com.cristianllanos/container)](https://central.sonatype.com/artifact/com.cristianllanos/container)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Install

```kotlin
dependencies {
    implementation("com.cristianllanos:container:0.4.0")
}
```

## Quick start

```kotlin
class Logger
class UserRepository(val logger: Logger)
class UserService(val repo: UserRepository)

val container = Container()
val service = container.resolve<UserService>() // resolves the entire tree
```

No registration needed for concrete classes. For interfaces, use explicit bindings:

```kotlin
val container = Container {
    singleton<NotificationService> { SlackNotificationService() }
    factory<PaymentGateway> { StripeGateway() }
    scoped<DbConnection> { DbConnection(resolve<Config>()) }
}
```

## Documentation

Full guides, examples, and API reference at **[cristianllanos.com/projects/kotlin-container](https://cristianllanos.com/projects/kotlin-container/)**:

- [Getting Started](https://cristianllanos.com/projects/kotlin-container/guide/) — install, first container, DSL builder
- [Bindings](https://cristianllanos.com/projects/kotlin-container/bindings/) — factory, singleton, scoped
- [Scopes](https://cristianllanos.com/projects/kotlin-container/scopes/) — lifecycles, dispose hooks, nested scopes, Android
- [Service Providers](https://cristianllanos.com/projects/kotlin-container/providers/) — modular registration
- [Advanced](https://cristianllanos.com/projects/kotlin-container/advanced/) — thread safety, callable injection, interface segregation
- [API Reference](https://cristianllanos.com/projects/kotlin-container/api/) — complete public API
- [Changelog](https://cristianllanos.com/projects/kotlin-container/changelog/) — release history

## License

MIT
