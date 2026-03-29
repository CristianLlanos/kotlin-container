package com.cristianllanos.container

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Default [AutoResolver] that uses Kotlin reflection to construct concrete classes
 * by recursively resolving their primary constructor parameters.
 */
class ReflectionAutoResolver : AutoResolver {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolve(type: Class<T>, resolver: Resolver): T {
        return autoResolve(type.kotlin, resolver) as T
    }

    private fun autoResolve(klass: KClass<*>, resolver: Resolver): Any {
        if (!isResolvableClass(klass)) {
            throw UnresolvableDependencyException("Unable to resolve dependency [${klass.simpleName}]")
        }

        val constructor = klass.primaryConstructor
            ?: throw UnresolvableDependencyException("Unable to resolve dependency [${klass.simpleName}]: no primary constructor")

        val args = resolveParameters(constructor.parameters, klass.simpleName ?: "unknown", resolver)
        return constructor.callBy(args)
    }

    private fun isResolvableClass(klass: KClass<*>): Boolean {
        if (klass.java.isPrimitive) return false
        if (klass == String::class) return false
        if (klass.java.isInterface) return false
        if (java.lang.reflect.Modifier.isAbstract(klass.java.modifiers)) return false
        return klass.primaryConstructor != null
    }
}
