package com.cristianllanos.container

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberFunctions

internal class Dependencies private constructor(
    private val parent: Dependencies?,
    private val autoResolver: AutoResolver,
) : Scope {

    companion object {
        fun make(autoResolver: AutoResolver): Container {
            val container = Dependencies(parent = null, autoResolver = autoResolver)
            container.singleton(Container::class.java) { this }
            return container
        }
    }

    private val bindings = ConcurrentHashMap<Class<*>, Binding<*>>()
    private val singletons = ConcurrentHashMap<Class<*>, Any>()
    private val resolving = ThreadLocal.withInitial { mutableSetOf<Class<*>>() }
    private val constructingSingletons = ThreadLocal.withInitial { mutableSetOf<Class<*>>() }
    private val scopedInstances = ConcurrentHashMap<Class<*>, ScopedEntry>()
    private val children = ConcurrentHashMap.newKeySet<Dependencies>()
    @Volatile private var closed = false

    private val isRoot get() = parent == null

    private class ScopedEntry(val instance: Any, val binding: Binding.Scoped<*>)

    override fun register(vararg providers: Any): Registrar {
        providers.forEach { provider ->
            val method = provider::class.declaredMemberFunctions
                .singleOrNull { it.name == "register" }
                ?: throw IllegalArgumentException(
                    "Provider [${provider::class.simpleName}] must have exactly one register() method"
                )

            val args = resolveParameters(method.parameters, provider::class.simpleName ?: "provider", this)
                .toMutableMap()
            method.parameters.find { it.kind == KParameter.Kind.INSTANCE }?.let { args[it] = provider }
            method.callBy(args)
        }
        return this
    }

    override fun <T : Any> factory(type: Class<T>, factory: Container.() -> T): Registrar {
        bindings[type] = Binding.Factory(factory)
        return this
    }

    override fun <T : Any> singleton(type: Class<T>, factory: Container.() -> T): Registrar {
        bindings[type] = Binding.Singleton(factory)
        return this
    }

    override fun <T : Any> scoped(type: Class<T>, factory: Container.() -> T): ScopedRegistration<T> {
        val binding = Binding.Scoped(factory)
        bindings[type] = binding
        return ScopedRegistration(binding, this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolve(type: Class<T>): T {
        if (!isRoot) check(!closed) { "Cannot resolve from a closed scope" }

        singletons[type]?.let { return it as T }

        val chain = resolving.get()
        check(chain.add(type)) {
            "Circular dependency detected while resolving [${type.simpleName}]. Resolution chain: ${chain.map { it.simpleName }}"
        }
        try {
            val local = bindings[type]
            if (local != null && type !in constructingSingletons.get()) return resolveBinding(local, type)

            parent?.findBinding(type)?.let { return resolveBinding(it, type) }

            return autoResolver.resolve(type, this)
        } finally {
            chain.remove(type)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> resolveBinding(binding: Binding<*>, type: Class<T>): T = when (binding) {
        is Binding.Factory -> binding.factory(this) as T
        is Binding.Singleton -> {
            if (bindings[type] === binding) {
                singletons[type] as? T ?: synchronized(binding) {
                    singletons[type] as? T ?: run {
                        // Allow the singleton's factory to resolve its own type via
                        // parent/auto-resolver instead of recursing into itself.
                        val constructing = constructingSingletons.get()
                        constructing.add(type)
                        resolving.get().remove(type)
                        try {
                            val instance = binding.factory(this)
                            singletons[type] = instance
                            instance as T
                        } finally {
                            constructing.remove(type)
                        }
                    }
                }
            } else {
                parent!!.resolve(type)
            }
        }
        is Binding.Scoped -> {
            if (isRoot) throw ScopeRequiredException(
                "Cannot resolve scoped binding [${type.simpleName}] without an active scope"
            )
            scopedInstances[type]?.instance as? T ?: synchronized(binding) {
                scopedInstances.getOrPut(type) {
                    ScopedEntry(binding.factory(this), binding)
                }.instance as T
            }
        }
    }

    private fun findBinding(type: Class<*>): Binding<*>? =
        bindings[type] ?: parent?.findBinding(type)

    override fun <T> call(callable: KCallable<T>): T {
        val args = resolveParameters(callable.parameters, callable.name, this)
        return callable.callBy(args)
    }

    override fun child(): Scope {
        if (!isRoot) check(!closed) { "Cannot create child scope from a closed scope" }
        val child = Dependencies(parent = this, autoResolver = autoResolver)
        children.add(child)
        return child
    }

    override fun close() {
        if (isRoot || closed) return
        closed = true

        val childrenSnapshot = children.toList()
        children.clear()
        childrenSnapshot.forEach { it.close() }

        var firstException: Throwable? = null
        for ((_, entry) in scopedInstances) {
            try {
                @Suppress("UNCHECKED_CAST")
                val explicitHook = (entry.binding as Binding.Scoped<Any>).onClose

                if (explicitHook != null) {
                    explicitHook(entry.instance)
                } else if (entry.instance is AutoCloseable) {
                    entry.instance.close()
                }
            } catch (e: Throwable) {
                if (firstException == null) firstException = e
                else firstException.addSuppressed(e)
            }
        }
        scopedInstances.clear()

        parent?.children?.remove(this)

        firstException?.let { throw it }
    }
}
