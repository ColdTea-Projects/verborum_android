package de.coldtea.verborum.core.profile.domain

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.auth.domain.AuthService
import de.coldtea.verborum.core.profile.domain.model.ReauthenticationRequiredException
import de.coldtea.verborum.core.profile.domain.usecase.api.DeleteAccountApiUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AccountDeletionServiceTest : BaseTest() {

    @MockK
    private lateinit var profileService: ProfileService

    @MockK
    private lateinit var deleteAccountApiUseCase: DeleteAccountApiUseCase

    @MockK
    private lateinit var authService: AuthService

    private lateinit var service: AccountDeletionService

    override fun setUp() {
        super.setUp()
        coEvery { profileService.getUserId() } returns USER_ID
        service = AccountDeletionService(profileService, deleteAccountApiUseCase, authService)
    }

    @Test
    fun `the server deletes first, and the local wipe runs only after it answered`() = runTest {
        service.deleteAccount()

        coVerifyOrder {
            deleteAccountApiUseCase.invoke(USER_ID)
            authService.completeAccountDeletion()
        }
    }

    @Test
    fun `an old login keeps all local data`() = runTest {
        coEvery { deleteAccountApiUseCase.invoke(USER_ID) } throws ReauthenticationRequiredException()

        try {
            service.deleteAccount()
        } catch (_: ReauthenticationRequiredException) {
        }

        coVerify(exactly = 0) { authService.completeAccountDeletion() }
    }

    @Test
    fun `an unknown result keeps all local data`() = runTest {
        coEvery { deleteAccountApiUseCase.invoke(USER_ID) } throws RuntimeException("timeout")

        try {
            service.deleteAccount()
        } catch (_: RuntimeException) {
        }

        coVerify(exactly = 0) { authService.completeAccountDeletion() }
    }

    private companion object {
        const val USER_ID = "user-id"
    }
}
