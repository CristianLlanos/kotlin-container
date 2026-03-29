package com.cristianllanos.container

interface Scope : Container, AutoCloseable

inline fun <R> Container.scope(block: (Scope) -> R): R {
    val scope = child()
    return scope.use { block(scope) }
}
