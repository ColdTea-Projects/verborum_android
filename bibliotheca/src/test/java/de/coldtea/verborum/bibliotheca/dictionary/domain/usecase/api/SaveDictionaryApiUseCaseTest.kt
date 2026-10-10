package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryRequest
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SaveDictionaryApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryApi: DictionaryApi

    private lateinit var useCase: SaveDictionaryApiUseCase

    private val sent = mutableListOf<DictionaryRequest>()

    override fun setUp() {
        super.setUp()
        useCase = SaveDictionaryApiUseCase(dictionaryApi)
    }

    private fun refused(error: String): Response<Unit> =
        Response.error(400, """{"status":400,"error":"$error"}""".toResponseBody("application/json".toMediaType()))

    @Test
    fun `an existing dictionary goes up without its sharing state`() = runTest {
        coEvery { dictionaryApi.createDictionary(capture(sent)) } returns Response.success(Unit)

        assertTrue(useCase.invoke(testDictionary(isPublic = false)).isSuccessful)

        assertEquals(listOf<Boolean?>(null), sent.map { it.isPublic })
    }

    @Test
    fun `a new dictionary states the local choice when the server asks for it`() = runTest {
        coEvery { dictionaryApi.createDictionary(capture(sent)) } returnsMany
            listOf(refused("InvalidRequestException"), Response.success(Unit))

        assertTrue(useCase.invoke(testDictionary(isPublic = false)).isSuccessful)

        assertEquals(listOf(null, false), sent.map { it.isPublic })
    }

    @Test
    fun `a member's private first dictionary goes up shared`() = runTest {
        coEvery { dictionaryApi.createDictionary(capture(sent)) } returnsMany listOf(
            refused("InvalidRequestException"),
            refused("SharingRequiredException"),
            Response.success(Unit),
        )

        assertTrue(useCase.invoke(testDictionary(isPublic = false)).isSuccessful)

        assertEquals(listOf(null, false, true), sent.map { it.isPublic })
    }

    @Test
    fun `any other refusal is returned as is`() = runTest {
        coEvery { dictionaryApi.createDictionary(capture(sent)) } returns refused("MethodArgumentNotValidException")

        assertEquals(400, useCase.invoke(testDictionary()).code())
        assertEquals(1, sent.size)
    }
}
