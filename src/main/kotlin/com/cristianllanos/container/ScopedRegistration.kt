package com.cristianllanos.container

class ScopedRegistration<T : Any> internal constructor(
    private val binding: Binding.Scoped<T>,
    private val registrar: Registrar,
) {
    fun onClose(action: (T) -> Unit): Registrar {
        binding.onClose = action
        return registrar
    }
}
