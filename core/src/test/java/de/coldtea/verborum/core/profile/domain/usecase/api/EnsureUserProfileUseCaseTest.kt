package de.coldtea.verborum.core.profile.domain.usecase.api

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.profile.data.api.UserApi
import de.coldtea.verborum.core.profile.data.api.model.UserProfileRequest
import de.coldtea.verborum.core.profile.data.api.model.UserProfileResponse
import de.coldtea.verborum.core.profile.testUserProfile
import de.coldtea.verborum.core.profile.testUserProfileResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.util.UUID

class EnsureUserProfileUseCaseTest : BaseTest() {

    @MockK
    private lateinit var userApi: UserApi

    private lateinit var useCase: EnsureUserProfileUseCase

    override fun setUp() {
        super.setUp()
        useCase = EnsureUserProfileUseCase(userApi)
    }

    private fun <T> error(code: Int): Response<T> =
        Response.error(code, "".toResponseBody("application/json".toMediaType()))

    // region existing profile
    @Test
    fun `returns the profile from users-me without creating one`() = runTest {
        coEvery { userApi.getMe() } returns Response.success(testUserProfileResponse(marketplaceAgreementAccepted = true))

        val profile = useCase.invoke(subject = SUBJECT, email = EMAIL, displayName = "Ada")

        assertEquals(testUserProfile(isMarketplaceMember = true), profile)
        coVerify(exactly = 0) { userApi.createProfile(any()) }
    }

    @Test(expected = HttpException::class)
    fun `throws without creating when users-me fails with anything but 404`() = runTest {
        coEvery { userApi.getMe() } returns error(500)

        try {
            useCase.invoke(subject = SUBJECT, email = EMAIL, displayName = null)
        } finally {
            coVerify(exactly = 0) { userApi.createProfile(any()) }
        }
    }
    // endregion

    // region no profile yet
    @Test
    fun `on 404 creates the profile with a fresh userId and the subject, then reloads it`() = runTest {
        coEvery { userApi.getMe() } returnsMany listOf(error(404), Response.success(testUserProfileResponse()))
        val body = slot<UserProfileRequest>()
        coEvery { userApi.createProfile(capture(body)) } returns Response.success(Unit)

        val profile = useCase.invoke(subject = SUBJECT, email = EMAIL, displayName = "  Ada  ")

        assertEquals(testUserProfile(), profile)
        // A random canonical UUID — never the subject (guide §4.3).
        assertNotEquals(SUBJECT, body.captured.userId)
        UUID.fromString(body.captured.userId)
        assertEquals(SUBJECT, body.captured.keycloakId)
        assertEquals(EMAIL, body.captured.email)
        assertEquals("Ada", body.captured.displayName)
    }

    @Test
    fun `omits a blank display name when creating`() = runTest {
        coEvery { userApi.getMe() } returnsMany listOf(error(404), Response.success(testUserProfileResponse()))
        val body = slot<UserProfileRequest>()
        coEvery { userApi.createProfile(capture(body)) } returns Response.success(Unit)

        useCase.invoke(subject = SUBJECT, email = EMAIL, displayName = " ")

        assertNull(body.captured.displayName)
    }

    @Test
    fun `a 409 on create recovers with users-me instead of failing`() = runTest {
        coEvery { userApi.getMe() } returnsMany listOf(error(404), Response.success(testUserProfileResponse()))
        coEvery { userApi.createProfile(any()) } returns error(409)

        assertEquals(testUserProfile(), useCase.invoke(subject = SUBJECT, email = EMAIL, displayName = null))
    }

    @Test(expected = IllegalStateException::class)
    fun `cannot create a profile without an email`() = runTest {
        coEvery { userApi.getMe() } returns error<UserProfileResponse>(404)

        try {
            useCase.invoke(subject = SUBJECT, email = null, displayName = null)
        } finally {
            coVerify(exactly = 0) { userApi.createProfile(any()) }
        }
    }
    // endregion

    private companion object {
        const val SUBJECT = "765a81ed-2612-4dcc-bf7a-3d5fecf3d0d6"
        const val EMAIL = "anna@example.com"
    }
}
