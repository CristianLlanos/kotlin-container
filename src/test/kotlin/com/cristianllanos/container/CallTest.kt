package com.cristianllanos.container

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

fun noParams(): String = "ok"

fun withSimpleDep(a: ServiceA): ServiceA = a

fun withComplexDeps(a: ServiceA, b: ServiceB): Pair<ServiceA, ServiceB> = Pair(a, b)

fun withOptionalPrimitive(a: ServiceA, label: String = "default"): String = label

fun withRequiredPrimitive(name: String): String = name

fun withUnregisteredAbstract(dep: RegisteredAbstractDependency): RegisteredAbstractDependency = dep

class Greeter {
    fun greet(service: ServiceA): ServiceA = service
    fun greetWithName(service: ServiceA, name: String = "world"): String = name
}

class CallTest {

    @Test
    fun `calls a function with no parameters`() {
        val container = Container()
        assertEquals("ok", container.call(::noParams))
    }

    @Test
    fun `calls a function resolving a simple dependency`() {
        val container = Container()
        assertIs<ServiceA>(container.call(::withSimpleDep))
    }

    @Test
    fun `calls a function resolving complex dependencies`() {
        val container = Container()
        val result = container.call(::withComplexDeps)
        assertIs<ServiceA>(result.first)
        assertIs<ServiceB>(result.second)
    }

    @Test
    fun `calls a function skipping optional primitive parameters`() {
        val container = Container()
        assertEquals("default", container.call(::withOptionalPrimitive))
    }

    @Test
    fun `fails when a required primitive parameter cannot be resolved`() {
        val container = Container()
        assertFailsWith<UnresolvableDependencyException> {
            container.call(::withRequiredPrimitive)
        }
    }

    @Test
    fun `fails when a parameter is an unregistered interface`() {
        val container = Container()
        assertFailsWith<UnresolvableDependencyException> {
            container.call(::withUnregisteredAbstract)
        }
    }

    @Test
    fun `calls a function with registered dependencies`() {
        val container = Container()
        container.singleton<ServiceA> { ServiceA() }

        val result = container.call(::withSimpleDep)
        assertIs<ServiceA>(result)
    }

    @Test
    fun `calls an instance method resolving its dependencies`() {
        val container = Container()
        val greeter = Greeter()

        assertIs<ServiceA>(container.call(greeter::greet))
    }

    @Test
    fun `calls an instance method skipping optional primitives`() {
        val container = Container()
        val greeter = Greeter()

        assertEquals("world", container.call(greeter::greetWithName))
    }
}
