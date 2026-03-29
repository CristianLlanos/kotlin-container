package com.cristianllanos.container

/**
 * Central dependency injection container that combines registration, resolution, and callable invocation.
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
