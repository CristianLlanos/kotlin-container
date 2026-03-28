package com.cristianllanos.container

interface Resolver {
    fun <T : Any> resolve(type: Class<T>): T
}

inline fun <reified T : Any> Resolver.resolve(): T =
    resolve(T::class.java)
