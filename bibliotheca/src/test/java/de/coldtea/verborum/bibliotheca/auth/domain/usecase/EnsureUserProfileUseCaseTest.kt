package de.coldtea.verborum.bibliotheca.auth.domain.usecase

import de.coldtea.verborum.bibliotheca.auth.data.api.UserApi
import de.coldtea.verborum.bibliotheca.auth.data.api.model.UserProfileRequest
import de.coldtea.verborum.bibliotheca.auth.data.api.model.UserProfileResponse
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class EnsureUserProfileUseCaseTest : BaseTest() {

    @MockK
    private lateinit var userApi: UserApi

    private lateinit var useCase: EnsureUserProfileUseCase

    override fun setUp() {
        super.setUp()
        useCase = EnsureUserProfileUseCase(userApi)
    }

    private fun notFound(): Response<UserProfileResponse> =
        Response.error(404, "".toResponseBody("application/json".toMediaType()))

    @Test
    fun `creates the profile with the subject as userId and keycloakId on 404`() = runTest {
        coEvery { userApi.getProfile("sub-1") } returns notFound()
        val body = slot<UserProfileRequest>()
        coEvery { userApi.createProfile(capture(body)) } returns Response.success(Unit)

        useCase.invoke(subject = "sub-1", email = "a@b.com", displayName = "Ada")

        assertEquals("sub-1", body.captured.userId)
        assertEquals("sub-1", body.captured.keycloakId)
        assertEquals("a@b.com", body.captured.email)
        assertEquals("Ada", body.captured.displayName)
    }

    @Test
    fun `does not create when the profile already exists`() = runTest {
        coEvery { userApi.getProfile("sub-1") } returns Response.success(UserProfileResponse())

        useCase.invoke(subject = "sub-1", email = null, displayName = null)

        coVerify(exactly = 0) { userApi.createProfile(any()) }
    }

    @Test
    fun `does not create when the lookup fails with a network error`() = runTest {
        coEvery { userApi.getProfile("sub-1") } throws RuntimeException("no network")

        useCase.invoke(subject = "sub-1", email = null, displayName = null)

        coVerify(exactly = 0) { userApi.createProfile(any()) }
    }
}
