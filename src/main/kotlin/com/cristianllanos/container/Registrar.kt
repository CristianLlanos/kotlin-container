package com.cristianllanos.container

interface Registrar {
    fun register(vararg providers: Any): Registrar
    fun <T : Any> factory(type: Class<T>, factory: Container.() -> T): Registrar
    fun <T : Any> singleton(type: Class<T>, factory: Container.() -> T): Registrar
    fun <T : Any> scoped(type: Class<T>, factory: Container.() -> T): ScopedRegistration<T>
}

inline fun <reified T : Any> Registrar.factory(noinline factory: Container.() -> T): Registrar =
    factory(T::class.java, factory)

inline fun <reified T : Any> Registrar.singleton(noinline factory: Container.() -> T): Registrar =
    singleton(T::class.java, factory)

inline fun <reified T : Any> Registrar.scoped(noinline factory: Container.() -> T): ScopedRegistration<T> =
    scoped(T::class.java, factory)

inline fun <reified T : Any> Registrar.factory(): Registrar =
    factory(T::class.java) { resolve() }

inline fun <reified T : Any> Registrar.singleton(): Registrar =
    singleton(T::class.java) { resolve() }

inline fun <reified T : Any> Registrar.scoped(): ScopedRegistration<T> =
    scoped(T::class.java) { resolve() }
