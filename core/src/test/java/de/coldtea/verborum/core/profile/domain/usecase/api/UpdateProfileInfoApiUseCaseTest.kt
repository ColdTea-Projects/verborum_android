package de.coldtea.verborum.core.profile.domain.usecase.api

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.profile.data.api.UserApi
import de.coldtea.verborum.core.profile.data.api.model.ProfileInfoRequest
import de.coldtea.verborum.core.profile.domain.model.InvalidProfileException
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class UpdateProfileInfoApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var userApi: UserApi

    private lateinit var useCase: UpdateProfileInfoApiUseCase

    override fun setUp() {
        super.setUp()
        useCase = UpdateProfileInfoApiUseCase(userApi)
    }

    private val request = ProfileInfoRequest(displayName = "Anna")

    @Test
    fun `succeeds silently on 201`() = runTest {
        coEvery { userApi.updateProfileInfo(request) } returns Response.success(201, Unit)

        useCase.invoke(request)
    }

    @Test
    fun `a 400 becomes InvalidProfileException carrying the errorDetail`() = runTest {
        val body = """{"status":400,"error":"InvalidProfileException","errorDetail":"displayName cannot be empty"}"""
        coEvery { userApi.updateProfileInfo(request) } returns
            Response.error(400, body.toResponseBody("application/json".toMediaType()))

        try {
            useCase.invoke(request)
            fail("expected InvalidProfileException")
        } catch (e: InvalidProfileException) {
            assertEquals("displayName cannot be empty", e.detail)
        }
    }

    @Test(expected = HttpException::class)
    fun `any other failure throws HttpException`() = runTest {
        coEvery { userApi.updateProfileInfo(request) } returns
            Response.error(404, "".toResponseBody("application/json".toMediaType()))

        useCase.invoke(request)
    }
}
