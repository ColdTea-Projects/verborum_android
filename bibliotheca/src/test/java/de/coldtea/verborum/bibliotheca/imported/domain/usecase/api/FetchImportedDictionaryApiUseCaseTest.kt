package de.coldtea.verborum.bibliotheca.imported.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryTagApi
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryTagResponse
import de.coldtea.verborum.bibliotheca.testDictionaryResponse
import de.coldtea.verborum.bibliotheca.testWordResponse
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class FetchImportedDictionaryApiUseCaseTest : BaseTest() {

    @MockK private lateinit var dictionaryApi: DictionaryApi
    @MockK private lateinit var dictionaryTagApi: DictionaryTagApi
    @MockK private lateinit var wordApi: WordApi

    private lateinit var useCase: FetchImportedDictionaryApiUseCase

    override fun setUp() {
        super.setUp()
        useCase = FetchImportedDictionaryApiUseCase(dictionaryApi, dictionaryTagApi, wordApi)
    }

    @Test
    fun `reads the dictionary, its normalised tags and its words with progress starting at 0`() = runTest {
        coEvery { dictionaryApi.getDictionary("d1") } returns
            testDictionaryResponse(dictionaryId = "d1", userId = "owner-sub").copy(name = "Kitchen", fromLang = "PL", toLang = "UK")
        coEvery { dictionaryTagApi.getDictionaryTags("d1") } returns
            listOf(DictionaryTagResponse(tag = " Food "), DictionaryTagResponse(tag = "food"), DictionaryTagResponse(tag = null))
        coEvery { wordApi.getWordsByDictionary("d1") } returns listOf(
            testWordResponse(wordId = "w1").copy(level = JsonPrimitive(5)),
            testWordResponse(wordId = null),
        )

        val content = useCase.invoke("d1", publisherName = "Anna", importedAt = 7_000L)!!

        assertEquals("owner-sub", content.dictionary.publisherId)
        assertEquals("Anna", content.dictionary.publisherName)
        assertEquals("Kitchen", content.dictionary.name)
        assertEquals(listOf("food"), content.dictionary.tags)
        assertEquals(7_000L, content.dictionary.importedAt)
        // The owner's level is not the user's; a word without an id is skipped.
        assertEquals(listOf("w1" to 0), content.words.map { it.wordId to it.level })
    }

    @Test
    fun `a dictionary made private or deleted reads as null`() = runTest {
        coEvery { dictionaryApi.getDictionary("gone") } throws
            HttpException(Response.error<Any>(404, "".toResponseBody("application/json".toMediaType())))

        assertNull(useCase.invoke("gone", publisherName = null, importedAt = 0L))
    }
}
