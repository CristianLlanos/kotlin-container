package com.cristianllanos.container

import kotlin.reflect.KCallable

internal class Dependencies private constructor(
    private val autoResolver: AutoResolver,
) : Container {

    companion object {
        fun make(autoResolver: AutoResolver = ReflectionAutoResolver()): Container =
            Dependencies(autoResolver)
    }

    private val definitions = mutableMapOf<Class<*>, Container.() -> Any>()
    private val singletons = mutableMapOf<Class<*>, Any>()

    override fun register(vararg providers: ServiceProvider): Registrar {
        providers.forEach { it.register(this) }
        return this
    }

    override fun <T : Any> factory(type: Class<T>, factory: Container.() -> T): Registrar {
        definitions[type] = factory
        return this
    }

    override fun <T : Any> singleton(type: Class<T>, factory: Container.() -> T): Registrar {
        definitions[type] = {
            @Suppress("UNCHECKED_CAST")
            singletons.getOrPut(type) { factory() } as T
        }
        return this
    }

    override fun <T> call(callable: KCallable<T>): T {
        val args = resolveParameters(callable.parameters, callable.name, this)
        return callable.callBy(args)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolve(type: Class<T>): T {
        val factory = definitions[type]
        if (factory != null) return factory(this) as T
        return autoResolver.resolve(type, this)
    }
}
