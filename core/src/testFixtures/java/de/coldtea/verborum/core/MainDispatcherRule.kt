package de.coldtea.verborum.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit [TestWatcher] rule that replaces [Dispatchers.Main] with a [TestDispatcher]
 * for the duration of each test.
 *
 * [UnconfinedTestDispatcher] is used by default so that coroutines are executed eagerly,
 * making state assertions straightforward without needing to manually advance the scheduler.
 * Swap in [kotlinx.coroutines.test.StandardTestDispatcher] when you need full control over
 * execution order (i.e. when you want to advance time manually with [TestDispatcher.scheduler]).
 */
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }
}
