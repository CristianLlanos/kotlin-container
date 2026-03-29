package com.cristianllanos.container

import kotlin.reflect.KCallable

/**
 * Invokes Kotlin callables with parameters resolved from the container.
 */
interface Caller {
    /** Calls [callable], resolving its parameters from the container. */
    fun <T> call(callable: KCallable<T>): T
}
