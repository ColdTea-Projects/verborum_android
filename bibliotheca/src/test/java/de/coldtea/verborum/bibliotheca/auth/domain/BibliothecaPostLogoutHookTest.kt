package de.coldtea.verborum.bibliotheca.auth.domain

import de.coldtea.verborum.bibliotheca.auth.domain.usecase.ClearLocalDataUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BibliothecaPostLogoutHookTest : BaseTest() {

    @MockK
    private lateinit var clearLocalDataUseCase: ClearLocalDataUseCase

    private lateinit var hook: BibliothecaPostLogoutHook

    override fun setUp() {
        super.setUp()
        hook = BibliothecaPostLogoutHook(clearLocalDataUseCase)
    }

    @Test
    fun `onLoggedOut wipes the local data`() = runTest {
        hook.onLoggedOut()

        coVerify(exactly = 1) { clearLocalDataUseCase.invoke() }
    }
}
