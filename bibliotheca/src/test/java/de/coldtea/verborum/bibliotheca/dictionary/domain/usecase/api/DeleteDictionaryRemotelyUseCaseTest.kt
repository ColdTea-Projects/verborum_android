package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.DictionaryDeletion
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.RestoreDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordByDictionaryIdUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class DeleteDictionaryRemotelyUseCaseTest : BaseTest() {

    @MockK private lateinit var deleteDictionaryApiUseCase: DeleteDictionaryApiUseCase
    @MockK(relaxed = true) private lateinit var deleteDictionaryUseCase: DeleteDictionaryUseCase
    @MockK(relaxed = true) private lateinit var deleteWordByDictionaryIdUseCase: DeleteWordByDictionaryIdUseCase
    @MockK(relaxed = true) private lateinit var restoreDictionaryUseCase: RestoreDictionaryUseCase

    private lateinit var useCase: DeleteDictionaryRemotelyUseCase

    override fun setUp() {
        super.setUp()
        useCase = DeleteDictionaryRemotelyUseCase(
            deleteDictionaryApiUseCase,
            deleteDictionaryUseCase,
            deleteWordByDictionaryIdUseCase,
            restoreDictionaryUseCase,
        )
    }

    private fun error(code: Int, body: String = ""): Response<Unit> =
        Response.error(code, body.toResponseBody("application/json".toMediaType()))

    @Test
    fun `a confirmed delete removes the dictionary and its words locally`() = runTest {
        coEvery { deleteDictionaryApiUseCase.invoke("d1") } returns Response.success(Unit)

        assertEquals(DictionaryDeletion.DELETED, useCase.invoke("d1"))
        coVerify(exactly = 1) { deleteWordByDictionaryIdUseCase.invoke("d1") }
        coVerify(exactly = 1) { deleteDictionaryUseCase.invoke("d1") }
    }

    @Test
    fun `the sharing rule refusal restores the dictionary and deletes nothing`() = runTest {
        coEvery { deleteDictionaryApiUseCase.invoke("d1") } returns
            error(400, """{"status":400,"error":"SharingRequiredException","errorDetail":"keep one shared"}""")

        assertEquals(DictionaryDeletion.SHARING_REQUIRED, useCase.invoke("d1"))
        coVerify(exactly = 1) { restoreDictionaryUseCase.invoke("d1") }
        coVerify(exactly = 0) { deleteWordByDictionaryIdUseCase.invoke(any()) }
        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
    }

    @Test
    fun `any other 400 is a refusal too, never retried`() = runTest {
        coEvery { deleteDictionaryApiUseCase.invoke("d1") } returns error(400)

        assertEquals(DictionaryDeletion.REFUSED, useCase.invoke("d1"))
        coVerify(exactly = 1) { restoreDictionaryUseCase.invoke("d1") }
    }

    @Test
    fun `offline or a server failure leaves everything for sync to retry`() = runTest {
        coEvery { deleteDictionaryApiUseCase.invoke("d1") } throws IOException("offline")
        assertEquals(DictionaryDeletion.PENDING, useCase.invoke("d1"))

        coEvery { deleteDictionaryApiUseCase.invoke("d1") } returns error(500)
        assertEquals(DictionaryDeletion.PENDING, useCase.invoke("d1"))

        coVerify(exactly = 0) { restoreDictionaryUseCase.invoke(any()) }
        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
    }
}
