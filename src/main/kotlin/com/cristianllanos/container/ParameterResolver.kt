package com.cristianllanos.container

import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal fun resolveParameters(
    parameters: List<KParameter>,
    contextName: String,
    resolver: Resolver,
): Map<KParameter, Any?> {
    val args = mutableMapOf<KParameter, Any?>()

    for (param in parameters) {
        if (param.kind == KParameter.Kind.INSTANCE) continue

        val paramClass = param.type.jvmErasure

        try {
            args[param] = resolver.resolve(paramClass.java)
        } catch (_: UnresolvableDependencyException) {
            if (param.isOptional) {
                continue
            } else {
                throw UnresolvableDependencyException(
                    "Unable to resolve dependency [$contextName]: " +
                        "cannot resolve parameter '${param.name}' of type [${paramClass.simpleName}]"
                )
            }
        }
    }

    return args
}
