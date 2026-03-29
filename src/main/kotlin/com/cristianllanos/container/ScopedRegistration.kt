package com.cristianllanos.container

/**
 * Handle returned when registering a scoped binding, allowing a cleanup hook to be attached.
 */
class ScopedRegistration<T : Any> internal constructor(
    private val binding: Binding.Scoped<T>,
    private val registrar: Registrar,
) {
    /** Registers [action] to be called with the scoped instance when the scope closes. */
    fun onClose(action: (T) -> Unit): Registrar {
        binding.onClose = action
        return registrar
    }
}
