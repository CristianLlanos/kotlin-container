package com.cristianllanos.container

import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class OneSimpleDependency(val service: SimpleService)

class OneComplexDependency(val a: SimpleService, val b: OneSimpleDependency)

class ResolvableComplexDependencyWithDefaultValuesInTheMiddle(
    val a: SimpleService,
    val name: String = "default",
    val b: OneSimpleDependency,
)

class AnotherComplexDependency(val dep: RegisteredAbstractDependency)

class DeepComplexDependency(val inner: AnotherComplexDependency)

class UnresolvableComplexDependency(val name: String, val service: SimpleService)

class ResolvableComplexDependency(
    val dep: RegisteredAbstractDependency,
    val count: Int = 0,
    val name: String = "default",
)

class AutoResolveTest {

    @Test
    fun `resolves a simple dependency`() {
        val container = Container()
        assertIs<OneSimpleDependency>(container.resolve<OneSimpleDependency>())
    }

    @Test
    fun `resolves a complex dependency`() {
        val container = Container()
        assertIs<OneComplexDependency>(container.resolve<OneComplexDependency>())
        assertIs<ResolvableComplexDependencyWithDefaultValuesInTheMiddle>(
            container.resolve<ResolvableComplexDependencyWithDefaultValuesInTheMiddle>()
        )
    }

    @Test
    fun `resolves a complex dependency when it depends on registered abstract dependencies`() {
        val container = Container()
        container.factory<RegisteredAbstractDependency> { AnotherSimpleDependency() }

        assertIs<AnotherComplexDependency>(container.resolve<AnotherComplexDependency>())
    }

    @Test
    fun `resolves a deep complex dependency when it depends on registered abstract dependencies`() {
        val container = Container()
        container.factory<RegisteredAbstractDependency> { AnotherSimpleDependency() }

        val resolved = container.resolve<DeepComplexDependency>()
        assertIs<DeepComplexDependency>(resolved)
        assertNotNull(resolved.inner.dep)
    }

    @Test
    fun `cannot resolve complex dependencies when it depends on primitive types`() {
        val container = Container()
        assertFailsWith<UnresolvableDependencyException> {
            container.resolve<UnresolvableComplexDependency>()
        }
    }

    @Test
    fun `can resolve complex dependencies when it depends on optional primitive types`() {
        val container = Container()
        container.factory<RegisteredAbstractDependency> { AnotherSimpleDependency() }

        assertIs<ResolvableComplexDependency>(container.resolve<ResolvableComplexDependency>())
    }

    @Test
    fun `resolves deep auto-resolution chain iteratively`() {
        val container = Container()
        val result = container.resolve<Level0>()
        assertNotNull(result.dep.dep.dep.dep.service)
    }
}
