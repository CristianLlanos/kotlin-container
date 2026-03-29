# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Thread-safe concurrent resolution: singletons created exactly once, scoped instances once per scope, per-thread circular dependency detection
- Convenience extensions: `resolveOrNull`, `has`, `lazy` on `Resolver`
- `Container { }` DSL builder for setup-in-one-shot
- `scope(vararg providers) { }` extension for scopes with pre-registered providers
- Concurrency test suite

### Changed
- Internal collections replaced with `ConcurrentHashMap`, `ThreadLocal`, `@Volatile`, and `synchronized` double-checked locking

## [0.3.1] - 2026-03-29

### Added
- Circular dependency detection with clear error messages and resolution chain reporting
- Deep auto-resolution tests

### Changed
- Improved parameter resolution handling
- Enforced single-assignment constraint on scoped `onClose` hooks

## [0.3.0] - 2026-03-28

### Added
- Parameterless registration overloads: `singleton<T>()`, `factory<T>()`, `scoped<T>()` for auto-resolved constructors
- Convention-based service providers with auto-resolved `register()` parameters

## [0.2.0] - 2026-03-28

### Added
- Scoped bindings with lifecycle management (`scoped`, `onClose`, `AutoCloseable` support)
- Nested scopes with cascading close
- `scope { }` block-based syntax for automatic cleanup
- `Scope` interface extending `Container` and `AutoCloseable`
- Published to Maven Central via Vanniktech plugin

## [0.1.0] - 2026-03-28

### Added
- Core container with `factory` and `singleton` bindings
- Auto-resolution of concrete classes via Kotlin reflection
- Service providers with `register()` convention
- Callable injection via `call()`
- Pluggable `AutoResolver` strategy
- Interface segregation: `Registrar`, `Resolver`, `Caller`, `Container`

[Unreleased]: https://github.com/CristianLlanos/kotlin-container/compare/v0.3.1...HEAD
[0.3.1]: https://github.com/CristianLlanos/kotlin-container/compare/v0.2.0...v0.3.1
[0.3.0]: https://github.com/CristianLlanos/kotlin-container/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/CristianLlanos/kotlin-container/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/CristianLlanos/kotlin-container/releases/tag/v0.1.0
