package de.coldtea.verborum.bibliotheca.dictionary.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryTagRequest
import de.coldtea.verborum.core.extensions.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/**
 * Integration test of [DictionaryTagApi] against a MockWebServer: verifies the real Retrofit
 * annotations (paths, verbs, body) and kotlinx-serialization parsing of the tag sub-resource,
 * without a live backend.
 */
@OptIn(ExperimentalSerializationApi::class)
class DictionaryTagApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: DictionaryTagApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(DictionaryTagApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `getDictionaryTags hits the right path and parses tag codes`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                [
                  {"tagId":"t1","dictionaryId":"d1","tag":"food_drink","createdAt":"2026-07-24T09:15:30.123Z"},
                  {"tagId":"t2","dictionaryId":"d1","tag":"a1","createdAt":"2026-07-24T09:15:31.000Z"}
                ]
                """.trimIndent()
            )
        )

        val tags = api.getDictionaryTags("d1")

        assertEquals(listOf("food_drink", "a1"), tags?.mapNotNull { it.tag })
        assertEquals("/dictionaries/d1/tags", server.takeRequest().path)
    }

    @Test
    fun `addDictionaryTag posts the tag body to the tags path`() = runTest {
        // The backend replies with an envelope, not the tag DTO; the client only needs the status.
        server.enqueue(
            MockResponse().setResponseCode(201)
                .setBody("""{"status":201,"message":"Saved successfully tag food_drink"}""")
        )

        val response = api.addDictionaryTag("d1", DictionaryTagRequest("food_drink"))

        assertTrue(response.isSuccessful)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/dictionaries/d1/tags", request.path)
        assertEquals("""{"tag":"food_drink"}""", request.body.readUtf8())
    }

    @Test
    fun `deleteDictionaryTag targets the tag in the path`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"status":200,"message":"Deleted successfully tag food_drink"}""")
        )

        val response = api.deleteDictionaryTag("d1", "food_drink")

        assertTrue(response.isSuccessful)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/dictionaries/d1/tags/food_drink", request.path)
    }
}
