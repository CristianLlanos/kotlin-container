package com.cristianllanos.container

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
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
    fun `register delegates to provider with auto-resolved params`() {
        val container = Container()
        container.singleton<String> { "hello" }

        class MyProvider {
            fun register(container: Container, greeting: String) {
                container.singleton<Int> { greeting.length }
            }
        }

        container.register(MyProvider())
        assertEquals(5, container.resolve<Int>())
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

    @Test
    fun `circular singleton dependency throws IllegalStateException`() {
        val container = Container()
        container.singleton<CircularA>()
        container.singleton<CircularB>()

        val error = assertFailsWith<IllegalStateException> {
            container.resolve<CircularA>()
        }
        assert(error.message!!.contains("Circular dependency")) {
            "Expected circular dependency message, got: ${error.message}"
        }
    }

    @Test
    fun `circular auto-resolved dependency throws IllegalStateException`() {
        val container = Container()

        val error = assertFailsWith<IllegalStateException> {
            container.resolve<CircularA>()
        }
        assert(error.message!!.contains("Circular")) {
            "Expected circular dependency message, got: ${error.message}"
        }
    }

    @Test
    fun `deep auto-resolution chain resolves without stack overflow`() {
        val container = Container()

        val result = container.resolve<Level0>()
        assertNotNull(result.dep)
        assertNotNull(result.dep.dep)
        assertNotNull(result.dep.dep.dep)
        assertNotNull(result.dep.dep.dep.dep)
        assertNotNull(result.dep.dep.dep.dep.service)
    }

    @Test
    fun `auto-resolving singleton does not self-reference loop`() {
        val container = Container()
        container.singleton<SimpleService>()

        val a = container.resolve<SimpleService>()
        val b = container.resolve<SimpleService>()
        assertSame(a, b)
    }

    @Test
    fun `auto-resolving singleton with dependencies does not self-reference loop`() {
        val container = Container()
        container.singleton<ServiceA>()
        container.singleton<ServiceB>()

        val result = container.resolve<ServiceB>()
        assertNotNull(result.a)

        val again = container.resolve<ServiceB>()
        assertSame(result, again)
    }

    @Test
    fun `many auto-resolving singletons resolve without stack overflow`() {
        val container = Container()
        container.singleton<Level4>()
        container.singleton<Level3>()
        container.singleton<Level2>()
        container.singleton<Level1>()
        container.singleton<Level0>()
        container.singleton<SimpleService>()

        val result = container.resolve<Level0>()
        assertNotNull(result.dep.dep.dep.dep.service)

        val again = container.resolve<Level0>()
        assertSame(result, again)
    }
}
