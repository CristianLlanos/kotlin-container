package com.cristianllanos.container

/**
 * A child container with its own lifecycle. Scoped bindings are instantiated per-scope
 * and cleaned up when the scope is closed.
 *
 * Thread-safe for concurrent resolution. [close] should be called exactly once.
 */
interface Scope : Container, AutoCloseable

/**
 * Opens a child [Scope], executes [block], and closes the scope automatically.
 *
 * ```kotlin
 * container.scope { scope ->
 *     val conn = scope.resolve<DbConnection>()
 *     // conn is cleaned up when scope closes
 * }
 * ```
 */
inline fun <R> Container.scope(block: (Scope) -> R): R {
    val scope = child()
    return scope.use { block(scope) }
}

/**
 * Opens a child [Scope], registers [providers], executes [block], and closes the scope automatically.
 *
 * ```kotlin
 * container.scope(RequestScopeProvider(request)) { scope ->
 *     scope.resolve<RequestHandler>().handle()
 * }
 * ```
 */
inline fun <R> Container.scope(vararg providers: Any, block: (Scope) -> R): R {
    val scope = child()
    return scope.use {
        scope.register(*providers)
        block(scope)
    }
}
