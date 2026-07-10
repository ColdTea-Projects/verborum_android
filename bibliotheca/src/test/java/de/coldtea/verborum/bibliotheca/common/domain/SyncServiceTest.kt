package de.coldtea.verborum.bibliotheca.common.domain

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.domain.usecases.SyncUserDictionariesUseCase
import de.coldtea.verborum.bibliotheca.common.domain.usecases.UploadPendingChangesUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncServiceTest : BaseTest() {

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var uploadPendingChangesUseCase: UploadPendingChangesUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var syncUserDictionariesUseCase: SyncUserDictionariesUseCase

    private lateinit var syncService: SyncService

    override fun setUp() {
        super.setUp()
        syncService = SyncService(uploadPendingChangesUseCase, syncUserDictionariesUseCase)
    }

    // region syncDictionaries

    @Test
    fun `syncDictionaries uploads pending changes before downloading`() = runTest {
        syncService.syncDictionaries()

        coVerifyOrder {
            uploadPendingChangesUseCase.invoke()
            syncUserDictionariesUseCase.invoke()
        }
    }

    @Test
    fun `syncDictionaries still downloads when the upload phase fails`() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        coEvery { uploadPendingChangesUseCase.invoke() } throws RuntimeException("upload down")

        syncService.syncDictionaries() // must not throw

        coVerify(exactly = 1) { syncUserDictionariesUseCase.invoke() }
        verify(exactly = 1) { Log.e("Upload error", "upload down") }
    }

    @Test
    fun `syncDictionaries swallows sync failures and logs them`() = runTest {
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
