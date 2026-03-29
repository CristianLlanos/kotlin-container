package com.cristianllanos.container

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Default [AutoResolver] that uses Kotlin reflection to construct concrete classes
 * by resolving their primary constructor parameters.
 */
class ReflectionAutoResolver : AutoResolver {

    private val resolving = mutableSetOf<Class<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolve(type: Class<T>, resolver: Resolver): T {
        return autoResolve(type.kotlin, resolver) as T
    }

    private fun autoResolve(klass: KClass<*>, resolver: Resolver): Any {
        val constructor = resolvableConstructor(klass)
            ?: throw UnresolvableDependencyException("Unable to resolve dependency [${klass.simpleName}]")

        check(resolving.add(klass.java)) {
            "Circular auto-resolution detected for [${klass.simpleName}]. Chain: ${resolving.map { it.simpleName }}"
        }

        try {
            val args = resolveParameters(constructor.parameters, klass.simpleName ?: "unknown", resolver)
            return constructor.callBy(args)!!
        } finally {
            resolving.remove(klass.java)
        }
    }

    private fun resolvableConstructor(klass: KClass<*>): kotlin.reflect.KFunction<*>? {
        if (klass.java.isPrimitive) return null
        if (klass == String::class) return null
        if (klass.java.isInterface) return null
        if (java.lang.reflect.Modifier.isAbstract(klass.java.modifiers)) return null
        return klass.primaryConstructor
    }
}
