package com.cristianllanos.container

internal sealed class Binding<T : Any> {
    abstract val factory: Container.() -> T

    class Factory<T : Any>(override val factory: Container.() -> T) : Binding<T>()
    class Singleton<T : Any>(override val factory: Container.() -> T) : Binding<T>()
    class Scoped<T : Any>(
        override val factory: Container.() -> T,
        var onClose: ((T) -> Unit)? = null,
    ) : Binding<T>()
}
