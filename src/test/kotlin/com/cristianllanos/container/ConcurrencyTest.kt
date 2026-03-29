package com.cristianllanos.container

import org.junit.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class ConcurrencyTest {

    private fun <T> raceThreads(count: Int = 20, action: (Int) -> T): List<T> {
        val barrier = CyclicBarrier(count)
        val results = arrayOfNulls<Any>(count)
        val errors = arrayOfNulls<Throwable>(count)
        val workers = (0 until count).map { i ->
            Thread {
                barrier.await()
                try {
                    results[i] = action(i)
                } catch (e: Throwable) {
                    errors[i] = e
                }
            }
        }
        workers.forEach { it.start() }
        workers.forEach { it.join() }
        errors.filterNotNull().firstOrNull()?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        return results.map { it as T }
    }

    @Test
    fun `singleton is created exactly once under concurrent resolution`() {
        val container = Container()
        val creationCount = AtomicInteger(0)

        container.singleton<SimpleService> {
            creationCount.incrementAndGet()
            Thread.sleep(10) // slow factory to widen the race window
            SimpleService()
        }

        val results = raceThreads<SimpleService> { container.resolve() }

        assertEquals(1, creationCount.get(), "Singleton factory should be invoked exactly once")
        results.forEach { assertSame(results[0], it, "All threads should receive the same instance") }
    }

    @Test
    fun `factory creates independent instances across threads`() {
        val container = Container()
        container.factory<SimpleService> { SimpleService() }

        val results = raceThreads<SimpleService> { container.resolve() }

        assertEquals(results.size, results.toSet().size, "Each thread should get a unique factory instance")
    }

    @Test
    fun `scoped instances are created once per scope under concurrency`() {
        val container = Container()
        val creationCount = AtomicInteger(0)

        container.scoped<SimpleService> {
            creationCount.incrementAndGet()
            SimpleService()
        }

        val scope = container.child()
        val results = raceThreads<SimpleService> { scope.resolve() }

        assertEquals(1, creationCount.get(), "Scoped factory should be invoked exactly once per scope")
        results.forEach { assertSame(results[0], it) }
    }

    @Test
    fun `concurrent resolution does not corrupt circular dependency detection`() {
        val container = Container()
        container.singleton<ServiceA> { ServiceA() }
        container.singleton<ServiceB> { ServiceB(resolve()) }

        val results = raceThreads<ServiceB> { container.resolve() }

        results.forEach { assertNotNull(it) }
    }

    @Test
    fun `close is safe while other threads resolve`() {
        val container = Container()
        container.scoped<SimpleService> { SimpleService() }

        val scope = container.child()
        scope.resolve<SimpleService>()

        val threads = 10
        val barrier = CyclicBarrier(threads + 1)

        val workers = (0 until threads).map {
            Thread {
                barrier.await()
                try {
                    scope.resolve<SimpleService>()
                } catch (_: IllegalStateException) {
                    // expected — scope may already be closed
                }
            }
        }

        workers.forEach { it.start() }
        barrier.await()
        scope.close()
        workers.forEach { it.join() }
    }
}
