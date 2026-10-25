package de.coldtea.verborum.core.profile.domain.usecase.api

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.profile.data.api.UserApi
import de.coldtea.verborum.core.profile.domain.model.ReauthenticationRequiredException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class DeleteAccountApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var userApi: UserApi

    private lateinit var useCase: DeleteAccountApiUseCase

    override fun setUp() {
        super.setUp()
        useCase = DeleteAccountApiUseCase(userApi)
    }

    private fun error(code: Int, body: String) =
        Response.error<Unit>(code, body.toResponseBody("application/json".toMediaType()))

    @Test
    fun `succeeds silently on 200`() = runTest {
        coEvery { userApi.deleteUser(USER_ID) } returns Response.success(200, Unit)

        useCase.invoke(USER_ID)

        coVerify(exactly = 1) { userApi.deleteUser(USER_ID) }
    }

    @Test(expected = ReauthenticationRequiredException::class)
    fun `a 403 ReauthenticationRequiredException asks for a new sign-in`() = runTest {
        coEvery { userApi.deleteUser(USER_ID) } returns
            error(403, """{"status":403,"error":"ReauthenticationRequiredException","errorDetail":"sign in again"}""")

        useCase.invoke(USER_ID)
    }

    @Test(expected = HttpException::class)
    fun `any other 403 is a failure, not a sign-in request`() = runTest {
        coEvery { userApi.deleteUser(USER_ID) } returns
            error(403, """{"status":403,"error":"ForbiddenOperationException","errorDetail":"not yours"}""")

        useCase.invoke(USER_ID)
    }

    @Test(expected = HttpException::class)
    fun `a 403 without a readable body is a failure`() = runTest {
        coEvery { userApi.deleteUser(USER_ID) } returns error(403, "")

        useCase.invoke(USER_ID)
    }

    @Test(expected = HttpException::class)
    fun `a server error throws HttpException`() = runTest {
        coEvery { userApi.deleteUser(USER_ID) } returns error(500, "")

        useCase.invoke(USER_ID)
    }

    private companion object {
        const val USER_ID = "user-id"
    }
}
