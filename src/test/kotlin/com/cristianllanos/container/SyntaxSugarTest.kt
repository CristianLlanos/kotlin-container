package com.cristianllanos.container

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SyntaxSugarTest {

    // -- resolveOrNull --

    @Test
    fun `resolveOrNull returns instance when registered`() {
        val container = Container()
        container.singleton<SimpleService> { SimpleService() }

        val result = container.resolveOrNull<SimpleService>()
        assertNotNull(result)
    }

    @Test
    fun `resolveOrNull returns null when unresolvable`() {
        val container = Container()

        val result = container.resolveOrNull<String>()
        assertNull(result)
    }

    // -- has --

    @Test
    fun `has returns true when type is resolvable`() {
        val container = Container()
        container.singleton<SimpleService> { SimpleService() }

        assertTrue(container.has<SimpleService>())
    }

    @Test
    fun `has returns false when type is unresolvable`() {
        val container = Container()

        assertFalse(container.has<String>())
    }

    // -- lazy --

    @Test
    fun `lazy defers resolution until first access`() {
        val container = Container()
        var created = false
        container.singleton<SimpleService> {
            created = true
            SimpleService()
        }

        val deferred = container.lazy<SimpleService>()
        assertFalse(created, "Factory should not run until .value is accessed")

        val instance = deferred.value
        assertTrue(created)
        assertNotNull(instance)
    }

    @Test
    fun `lazy returns same instance on subsequent access`() {
        val container = Container()
        container.singleton<SimpleService> { SimpleService() }

        val deferred = container.lazy<SimpleService>()
        assertSame(deferred.value, deferred.value)
    }

    // -- scope with providers --

    @Test
    fun `scope with providers registers and resolves within scope`() {
        val container = Container()

        class ScopeProvider {
            fun register(container: Container) {
                container.singleton<SimpleService> { SimpleService() }
            }
        }

        val result = container.scope(ScopeProvider()) { scope ->
            scope.resolve<SimpleService>()
        }

        assertNotNull(result)
    }

    // -- Container DSL builder --

    @Test
    fun `Container DSL builder registers bindings`() {
        val container = Container {
            singleton<ServiceA> { ServiceA() }
            factory<ServiceB> { ServiceB(resolve()) }
        }

        val serviceA = container.resolve<ServiceA>()
        val serviceB = container.resolve<ServiceB>()
        assertNotNull(serviceA)
        assertNotNull(serviceB)
        assertSame(serviceA, serviceB.a)
    }

    @Test
    fun `Container DSL builder uses custom auto-resolver`() {
        val custom = object : AutoResolver {
            override fun <T : Any> resolve(type: Class<T>, resolver: Resolver): T {
                throw UnresolvableDependencyException("custom resolver rejects all")
            }
        }

        val container = Container(custom) {
            singleton<SimpleService> { SimpleService() }
        }

        // Registered binding works
        assertNotNull(container.resolve<SimpleService>())
        // Unregistered type hits the custom resolver, which rejects
        assertFalse(container.has<ServiceA>())
    }
}
