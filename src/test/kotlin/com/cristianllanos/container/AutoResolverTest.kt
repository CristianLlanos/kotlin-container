package com.cristianllanos.container

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StubService(val label: String)

class CustomAutoResolver : AutoResolver {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolve(type: Class<T>, resolver: Resolver): T {
        if (type == StubService::class.java) {
            return StubService("from-custom-resolver") as T
        }
        throw UnresolvableDependencyException("Cannot resolve [${type.simpleName}]")
    }
}

class AutoResolverTest {

    @Test
    fun `uses custom auto resolver for unregistered types`() {
        val container = Container(CustomAutoResolver())
        val resolved = container.resolve<StubService>()
        assertIs<StubService>(resolved)
        assertEquals("from-custom-resolver", resolved.label)
    }

    @Test
    fun `registered types take precedence over custom auto resolver`() {
        val container = Container(CustomAutoResolver())
        container.singleton<StubService> { StubService("from-registration") }

        val resolved = container.resolve<StubService>()
        assertEquals("from-registration", resolved.label)
    }

    @Test
    fun `default auto resolver uses reflection`() {
        val container = Container()
        assertIs<SimpleService>(container.resolve<SimpleService>())
    }
}
