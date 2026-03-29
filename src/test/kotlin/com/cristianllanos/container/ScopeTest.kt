package com.cristianllanos.container

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ScopeTest {

    @Test
    fun `scoped binding returns same instance within scope`() {
        val container = Container()
        container.scoped<List<String>> { mutableListOf() }

        val scope = container.child()
        val a = scope.resolve<List<String>>()
        val b = scope.resolve<List<String>>()
        assertSame(a, b)
    }

    @Test
    fun `different scopes get different instances`() {
        val container = Container()
        container.scoped<List<String>> { mutableListOf() }

        val scope1 = container.child()
        val scope2 = container.child()
        val a = scope1.resolve<List<String>>()
        val b = scope2.resolve<List<String>>()
        assertNotSame(a, b)
    }

    @Test
    fun `nested scopes get their own instances`() {
        val container = Container()
        container.scoped<List<String>> { mutableListOf() }

        val parent = container.child()
        val child = parent.child()
        val parentInstance = parent.resolve<List<String>>()
        val childInstance = child.resolve<List<String>>()
        assertNotSame(parentInstance, childInstance)
    }

    @Test
    fun `scope resolves singletons from root`() {
        val container = Container()
        container.singleton<List<String>> { mutableListOf() }

        val scope = container.child()
        val fromScope = scope.resolve<List<String>>()
        val fromRoot = container.resolve<List<String>>()
        assertSame(fromRoot, fromScope)
    }

    @Test
    fun `scope resolves factories as new instances`() {
        val container = Container()
        container.factory<List<String>> { mutableListOf() }

        val scope = container.child()
        val a = scope.resolve<List<String>>()
        val b = scope.resolve<List<String>>()
        assertNotSame(a, b)
    }

    @Test
    fun `resolving scoped from root throws ScopeRequiredException`() {
        val container = Container()
        container.scoped<List<String>> { mutableListOf() }

        assertFailsWith<ScopeRequiredException> {
            container.resolve<List<String>>()
        }
    }

    @Test
    fun `onClose hook runs on scope close`() {
        var disposed = false
        val container = Container()
        container.scoped<List<String>> { mutableListOf() }.onClose { disposed = true }

        val scope = container.child()
        scope.resolve<List<String>>()
        scope.close()
        assertTrue(disposed)
    }

    @Test
    fun `AutoCloseable instances are auto-closed`() {
        var closed = false
        val container = Container()
        container.scoped<AutoCloseable> { AutoCloseable { closed = true } }

        val scope = container.child()
        scope.resolve<AutoCloseable>()
        scope.close()
        assertTrue(closed)
    }

    @Test
    fun `explicit onClose prevents auto-close`() {
        var autoCloseCount = 0
        var explicitCloseCount = 0

        val container = Container()
        container.scoped<AutoCloseable> {
            AutoCloseable { autoCloseCount++ }
        }.onClose { explicitCloseCount++ }

        val scope = container.child()
        scope.resolve<AutoCloseable>()
        scope.close()
        assertEquals(0, autoCloseCount)
        assertEquals(1, explicitCloseCount)
    }

    @Test
    fun `parent close cascades to children`() {
        val closeOrder = mutableListOf<String>()
        val container = Container()
        container.scoped<String> { "value" }.onClose { closeOrder.add("parent") }
        container.scoped<Int> { 42 }.onClose { closeOrder.add("child") }

        val parent = container.child()
        val child = parent.child()
        parent.resolve<String>()
        child.resolve<Int>()
        parent.close()

        assertEquals(listOf("child", "parent"), closeOrder)
    }

    @Test
    fun `resolving from closed scope throws`() {
        val container = Container()
        container.scoped<List<String>> { mutableListOf() }

        val scope = container.child()
        scope.close()

        assertFailsWith<IllegalStateException> {
            scope.resolve<List<String>>()
        }
    }

    @Test
    fun `ad-hoc bindings on scope`() {
        val container = Container()

        val scope = container.child()
        scope.singleton<String> { "scoped-value" }

        assertEquals("scoped-value", scope.resolve<String>())
        assertFailsWith<UnresolvableDependencyException> {
            container.resolve<String>()
        }
    }

    @Test
    fun `block-based scope auto-closes`() {
        var disposed = false
        val container = Container()
        container.scoped<List<String>> { mutableListOf() }.onClose { disposed = true }

        container.scope { scope ->
            scope.resolve<List<String>>()
        }
        assertTrue(disposed)
    }

    @Test
    fun `scoped binding can resolve other bindings`() {
        val container = Container()
        container.singleton<String> { "hello" }
        container.scoped<Pair<String, Int>> { Pair(resolve(), 42) }

        val scope = container.child()
        val result = scope.resolve<Pair<String, Int>>()
        assertEquals("hello", result.first)
        assertEquals(42, result.second)
    }

    @Test
    fun `scope auto-resolves concrete classes`() {
        val container = Container()
        val scope = container.child()
        val service = scope.resolve<SimpleService>()
        assertTrue(service is SimpleService)
    }

    @Test
    fun `singletons are consistent across scopes`() {
        val container = Container()
        container.singleton<List<String>> { mutableListOf() }

        val scope1 = container.child()
        val scope2 = container.child()
        assertSame(scope1.resolve<List<String>>(), scope2.resolve<List<String>>())
    }

    @Test
    fun `close is idempotent`() {
        var closeCount = 0
        val container = Container()
        container.scoped<String> { "value" }.onClose { closeCount++ }

        val scope = container.child()
        scope.resolve<String>()
        scope.close()
        scope.close()
        assertEquals(1, closeCount)
    }

    @Test
    fun `creating child from closed scope throws`() {
        val container = Container()
        val scope = container.child()
        scope.close()

        assertFailsWith<IllegalStateException> {
            scope.child()
        }
    }

    @Test
    fun `onClose does not run for unresolved scoped bindings`() {
        var disposed = false
        val container = Container()
        container.scoped<String> { "value" }.onClose { disposed = true }

        val scope = container.child()
        // Never resolve the binding
        scope.close()
        assertTrue(!disposed)
    }
}
