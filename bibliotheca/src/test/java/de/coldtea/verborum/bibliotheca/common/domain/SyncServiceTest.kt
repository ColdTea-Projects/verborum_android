package de.coldtea.verborum.bibliotheca.common.domain

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.domain.usecases.SyncUserDictionariesUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncServiceTest : BaseTest() {

    // invoke returns a non-Unit value — stubbed per test with coEvery.
    @MockK
    private lateinit var syncUserDictionariesUseCase: SyncUserDictionariesUseCase

    private lateinit var syncService: SyncService

    override fun setUp() {
        super.setUp()
        syncService = SyncService(syncUserDictionariesUseCase)
    }

    // region syncDictionaries

    @Test
    fun `syncDictionaries delegates to SyncUserDictionariesUseCase`() = runTest {
        coEvery { syncUserDictionariesUseCase.invoke() } returns null

        syncService.syncDictionaries()

        coVerify(exactly = 1) { syncUserDictionariesUseCase.invoke() }
    }

    @Test
    fun `syncDictionaries swallows use case failures and logs them`() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        coEvery { syncUserDictionariesUseCase.invoke() } throws RuntimeException("network down")

        syncService.syncDictionaries() // must not throw

        verify(exactly = 1) { Log.e("Sync error", "network down") }
    }

    @Test
    fun `syncDictionaries logs an empty message when the exception has none`() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        coEvery { syncUserDictionariesUseCase.invoke() } throws RuntimeException()

        syncService.syncDictionaries()

        verify(exactly = 1) { Log.e("Sync error", "") }
    }

    // endregion
}
