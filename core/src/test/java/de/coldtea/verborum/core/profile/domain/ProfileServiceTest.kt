package de.coldtea.verborum.core.profile.domain

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.profile.data.api.model.ProfileInfoRequest
import de.coldtea.verborum.core.profile.domain.model.MarketplaceTerms
import de.coldtea.verborum.core.profile.domain.model.SessionIdentity
import de.coldtea.verborum.core.profile.domain.usecase.api.EnsureUserProfileUseCase
import de.coldtea.verborum.core.profile.domain.usecase.api.UpdateProfileInfoApiUseCase
import de.coldtea.verborum.core.profile.domain.usecase.local.GetSessionIdentityUseCase
import de.coldtea.verborum.core.profile.testProfileUi
import de.coldtea.verborum.core.profile.testUserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileServiceTest : BaseTest() {

    @MockK
    private lateinit var ensureUserProfileUseCase: EnsureUserProfileUseCase

    @MockK
    private lateinit var updateProfileInfoApiUseCase: UpdateProfileInfoApiUseCase

    @MockK
    private lateinit var getSessionIdentityUseCase: GetSessionIdentityUseCase

    private lateinit var service: ProfileService

    override fun setUp() {
        super.setUp()
        every { getSessionIdentityUseCase.invoke() } returns SessionIdentity(SUBJECT, EMAIL, "Ada")
        coEvery { ensureUserProfileUseCase.invoke(SUBJECT, EMAIL, "Ada") } returns testUserProfile()
        service = ProfileService(ensureUserProfileUseCase, updateProfileInfoApiUseCase, getSessionIdentityUseCase)
    }

    // region refresh & observe
    @Test
    fun `observeProfile is null until a profile is loaded`() = runTest {
        assertNull(service.observeProfile().first())
    }

    @Test
    fun `refreshProfile loads via the session identity and publishes it`() = runTest {
        val loaded = service.refreshProfile()

        assertEquals(testProfileUi(), loaded)
        assertEquals(testProfileUi(), service.observeProfile().first())
    }

    @Test(expected = IllegalStateException::class)
    fun `refreshProfile fails when no one is signed in`() = runTest {
        every { getSessionIdentityUseCase.invoke() } returns null

        service.refreshProfile()
    }

    @Test
    fun `clear forgets the profile`() = runTest {
        service.refreshProfile()

        service.clear()

        assertNull(service.observeProfile().first())
    }
    // endregion

    // region update
    @Test
    fun `accepting the terms sends the current terms version, then reloads`() = runTest {
        service.updateProfile(displayName = "Anna", marketplaceAgreementAccepted = true)

        coVerifyOrder {
            updateProfileInfoApiUseCase.invoke(
                ProfileInfoRequest(
                    displayName = "Anna",
                    marketplaceAgreementAccepted = true,
                    marketplaceAgreementVersion = MarketplaceTerms.VERSION,
                )
            )
            ensureUserProfileUseCase.invoke(SUBJECT, EMAIL, "Ada")
        }
    }

    @Test
    fun `withdrawing sends no terms version`() = runTest {
        service.updateProfile(displayName = null, marketplaceAgreementAccepted = false)

        coVerify { updateProfileInfoApiUseCase.invoke(ProfileInfoRequest(marketplaceAgreementAccepted = false)) }
    }

    @Test
    fun `a rename alone touches nothing else`() = runTest {
        service.updateProfile(displayName = "Anna Bauer", marketplaceAgreementAccepted = null)

        coVerify { updateProfileInfoApiUseCase.invoke(ProfileInfoRequest(displayName = "Anna Bauer")) }
    }
    // endregion

    // region userId
    @Test
    fun `getUserId uses the loaded profile without another call`() = runTest {
        service.refreshProfile()

        assertEquals("4140c0de-0000-4000-8000-0000000000bb", service.getUserId())
        coVerify(exactly = 1) { ensureUserProfileUseCase.invoke(any(), any(), any()) }
    }

    @Test
    fun `getUserId loads the profile first when none is known`() = runTest {
        assertEquals("4140c0de-0000-4000-8000-0000000000bb", service.getUserId())
    }
    // endregion

    private companion object {
        const val SUBJECT = "765a81ed-2612-4dcc-bf7a-3d5fecf3d0d6"
        const val EMAIL = "anna@example.com"
    }
}
