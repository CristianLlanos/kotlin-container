package com.cristianllanos.container

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class DependenciesTest {

    @Test
    fun `factory returns new instance each time`() {
        val container = Container()
        container.factory<List<String>> { mutableListOf() }

        val a = container.resolve<List<String>>()
        val b = container.resolve<List<String>>()
        assertNotSame(a, b)
    }

    @Test
    fun `singleton returns same instance each time`() {
        val container = Container()
        container.singleton<List<String>> { mutableListOf() }

        val a = container.resolve<List<String>>()
        val b = container.resolve<List<String>>()
        assertSame(a, b)
    }

    @Test
    fun `resolve throws for unregistered type`() {
        val container = Container()
        assertFailsWith<UnresolvableDependencyException> {
            container.resolve<String>()
        }
    }

    @Test
    fun `register delegates to service provider`() {
        val container = Container()
        val provider = object : ServiceProvider {
            override fun register(container: Container) {
                container.singleton<String> { "hello" }
            }
        }
        container.register(provider)
        assertEquals("hello", container.resolve<String>())
    }

    @Test
    fun `resolve injects dependencies between services`() {
        val container = Container()
        container.singleton<String> { "dependency" }
        container.factory<Pair<String, Int>> { Pair(resolve(), 42) }

        val result = container.resolve<Pair<String, Int>>()
        assertEquals("dependency", result.first)
        assertEquals(42, result.second)
    }
}
