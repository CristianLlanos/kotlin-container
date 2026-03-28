package com.cristianllanos.container

interface AutoResolver {
    fun <T : Any> resolve(type: Class<T>, resolver: Resolver): T
}

inline fun <reified T : Any> AutoResolver.resolve(resolver: Resolver): T =
    resolve(T::class.java, resolver)
