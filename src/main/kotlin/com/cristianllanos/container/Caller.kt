package com.cristianllanos.container

import kotlin.reflect.KCallable

interface Caller {
    fun <T> call(callable: KCallable<T>): T
}
