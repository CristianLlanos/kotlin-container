package com.cristianllanos.container

/**
 * Registers dependency bindings into the container.
 */
interface Registrar {
    /**
     * Registers one or more service providers. Each provider must have exactly one `register()` method
     * whose parameters are resolved from the container.
     */
    fun register(vararg providers: Any): Registrar

    /** Registers a factory binding that creates a new instance of [T] on every resolution. */
    fun <T : Any> factory(type: Class<T>, factory: Container.() -> T): Registrar

    /** Registers a singleton binding that creates [T] once and caches it for subsequent resolutions. */
    fun <T : Any> singleton(type: Class<T>, factory: Container.() -> T): Registrar

    /** Registers a scoped binding whose instance is tied to a [Scope] lifecycle. */
    fun <T : Any> scoped(type: Class<T>, factory: Container.() -> T): ScopedRegistration<T>
}

/** Reified overload of [Registrar.factory] with an explicit factory lambda. */
inline fun <reified T : Any> Registrar.factory(noinline factory: Container.() -> T): Registrar =
    factory(T::class.java, factory)

/** Reified overload of [Registrar.singleton] with an explicit factory lambda. */
inline fun <reified T : Any> Registrar.singleton(noinline factory: Container.() -> T): Registrar =
    singleton(T::class.java, factory)

/** Reified overload of [Registrar.scoped] with an explicit factory lambda. */
inline fun <reified T : Any> Registrar.scoped(noinline factory: Container.() -> T): ScopedRegistration<T> =
    scoped(T::class.java, factory)

/** Registers a factory binding for [T] using auto-resolution. */
inline fun <reified T : Any> Registrar.factory(): Registrar =
    factory(T::class.java) { resolve() }

/** Registers a singleton binding for [T] using auto-resolution. */
inline fun <reified T : Any> Registrar.singleton(): Registrar =
    singleton(T::class.java) { resolve() }

/** Registers a scoped binding for [T] using auto-resolution. */
inline fun <reified T : Any> Registrar.scoped(): ScopedRegistration<T> =
    scoped(T::class.java) { resolve() }
