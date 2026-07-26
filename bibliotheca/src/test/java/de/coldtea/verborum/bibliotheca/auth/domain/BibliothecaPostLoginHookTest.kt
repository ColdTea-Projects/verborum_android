package de.coldtea.verborum.bibliotheca.auth.domain

import de.coldtea.verborum.bibliotheca.auth.domain.usecase.MigrateGuestDataUseCase
import de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BibliothecaPostLoginHookTest : BaseTest() {

    @MockK
    private lateinit var migrateGuestDataUseCase: MigrateGuestDataUseCase

    @MockK
    private lateinit var syncScheduler: SyncScheduler

    private lateinit var hook: BibliothecaPostLoginHook

    override fun setUp() {
        super.setUp()
        hook = BibliothecaPostLoginHook(migrateGuestDataUseCase, syncScheduler)
    }

    @Test
    fun `onLoginCompleted migrates the guest data before requesting a sync`() = runTest {
        hook.onLoginCompleted(SUBJECT)

        // Order matters: migrating first means the sync uploads rows already owned by the subject.
        coVerifyOrder {
            migrateGuestDataUseCase.invoke(SUBJECT)
            syncScheduler.requestImmediateSync(uploadOnly = false)
        }
    }

    @Test
    fun `onLoginCompleted requests a full reconcile, not an upload-only sync`() = runTest {
        hook.onLoginCompleted(SUBJECT)

        verify(exactly = 1) { syncScheduler.requestImmediateSync(uploadOnly = false) }
        verify(exactly = 0) { syncScheduler.requestImmediateSync(uploadOnly = true) }
    }

    private companion object {
        const val SUBJECT = "e7c1f0e2-subject"
    }
}
