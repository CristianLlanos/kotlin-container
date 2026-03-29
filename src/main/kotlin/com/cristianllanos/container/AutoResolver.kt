package com.cristianllanos.container

/**
 * Strategy for resolving types that have no explicit binding in the container.
 */
interface AutoResolver {
    /** Resolves an instance of [type], using [resolver] to satisfy constructor dependencies. */
    fun <T : Any> resolve(type: Class<T>, resolver: Resolver): T
}

/** Reified overload of [AutoResolver.resolve]. */
inline fun <reified T : Any> AutoResolver.resolve(resolver: Resolver): T =
    resolve(T::class.java, resolver)
