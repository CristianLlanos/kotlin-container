package com.cristianllanos.container

/**
 * Resolves dependencies from the container by type. Thread-safe for concurrent access.
 */
interface Resolver {
    /** Resolves an instance of [type] from the container. */
    fun <T : Any> resolve(type: Class<T>): T
}

/** Reified overload of [Resolver.resolve]. */
inline fun <reified T : Any> Resolver.resolve(): T =
    resolve(T::class.java)

/** Resolves [T] or returns `null` if the type is not resolvable. Note: triggers factory/singleton creation on success. */
inline fun <reified T : Any> Resolver.resolveOrNull(): T? =
    try { resolve(T::class.java) } catch (_: UnresolvableDependencyException) { null }

/** Returns `true` if [T] can be resolved from this resolver. */
inline fun <reified T : Any> Resolver.has(): Boolean =
    resolveOrNull<T>() != null

/** Returns a [Lazy] that resolves [T] on first access. Do not store beyond the receiver's lifetime when used with a [Scope]. */
inline fun <reified T : Any> Resolver.lazy(): Lazy<T> =
    lazy { resolve(T::class.java) }
