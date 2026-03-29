package com.cristianllanos.container

/**
 * Central dependency injection container that combines registration, resolution, and callable invocation.
 *
 * Implementations are thread-safe: [resolve] may be called concurrently from multiple threads.
 * Registration should happen during a single-threaded setup phase.
 *
 * ```kotlin
 * val container = Container()
 * container.singleton<Logger> { ConsoleLogger() }
 * val logger = container.resolve<Logger>()
 * ```
 */
interface Container : Registrar, Resolver, Caller {
    /** Creates a child [Scope] that inherits bindings from this container. */
    fun child(): Scope
}

/**
 * Creates a new [Container] with the given [autoResolver] strategy for unregistered concrete types.
 *
 * @param autoResolver strategy for resolving types not explicitly registered. Defaults to [ReflectionAutoResolver].
 */
fun Container(autoResolver: AutoResolver = ReflectionAutoResolver()): Container =
    Dependencies.make(autoResolver)

/**
 * Creates a new [Container] and configures it with [init].
 *
 * ```kotlin
 * val container = Container {
 *     singleton<Logger> { ConsoleLogger() }
 *     factory<PaymentGateway> { StripeGateway() }
 * }
 * ```
 */
fun Container(
    autoResolver: AutoResolver = ReflectionAutoResolver(),
    init: Container.() -> Unit,
): Container = Container(autoResolver).apply(init)
