package com.cristianllanos.container

/** Thrown when the container cannot resolve a requested dependency. */
class UnresolvableDependencyException(message: String) : RuntimeException(message)
