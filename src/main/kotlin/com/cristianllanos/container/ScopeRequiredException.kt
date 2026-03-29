package com.cristianllanos.container

/** Thrown when a scoped binding is resolved without an active [Scope]. */
class ScopeRequiredException(message: String) : RuntimeException(message)
