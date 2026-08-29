package de.coldtea.verborum.core.ui

import de.coldtea.verborum.core.BaseTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseViewModelTest : BaseTest() {

    // observe is a protected member extension — a minimal subclass exposes it to the test.
    private class TestViewModel : BaseViewModel() {
        fun <T> observeIn(
            flow: Flow<T>,
            onSuccess: suspend (T) -> Unit,
            onCompleted: (suspend () -> Unit)? = null,
            onError: (suspend (Throwable) -> Unit)? = null,
        ) = flow.observe(onSuccess = onSuccess, onCompleted = onCompleted, onError = onError)
    }

    private lateinit var viewModel: TestViewModel

    override fun setUp() {
        super.setUp()
        viewModel = TestViewModel()
    }

    @Test
    fun `observe delivers every emission to onSuccess`() = runTest {
        val received = mutableListOf<Int>()

        viewModel.observeIn(flowOf(1, 2, 3), onSuccess = { received += it })

        assertEquals(listOf(1, 2, 3), received)
    }

    @Test
    fun `observe routes an upstream failure to onError`() = runTest {
        var error: Throwable? = null

        viewModel.observeIn(
            flow<Int> { throw IllegalStateException("boom") },
            onSuccess = {},
            onError = { error = it },
        )

        assertEquals("boom", error?.message)
    }

    @Test
    fun `observe routes an exception thrown inside onSuccess to onError`() = runTest {
        var error: Throwable? = null

        viewModel.observeIn(
            flowOf(1),
            onSuccess = { throw IllegalStateException("mapping blew up") },
            onError = { error = it },
        )

        // Regression guard: catch is exception-transparent, so onSuccess must run upstream of it —
        // a throw from a collect block would skip onError and leave the screen stuck on Loading.
        assertEquals("mapping blew up", error?.message)
    }

    @Test
    fun `observe invokes onCompleted when the flow finishes`() = runTest {
        var completed = false

        viewModel.observeIn(flowOf(1), onSuccess = {}, onCompleted = { completed = true })

        assertTrue(completed)
    }
}
