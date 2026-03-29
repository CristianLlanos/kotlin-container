package com.cristianllanos.container

/**
 * Resolves dependencies from the container by type.
 */
interface Resolver {
    /** Resolves an instance of [type] from the container. */
    fun <T : Any> resolve(type: Class<T>): T
}

/** Reified overload of [Resolver.resolve]. */
inline fun <reified T : Any> Resolver.resolve(): T =
    resolve(T::class.java)
