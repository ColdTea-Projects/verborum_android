package de.coldtea.verborum.bibliotheca.common.data.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryResponse
import de.coldtea.verborum.bibliotheca.word.data.api.model.WordResponse
import de.coldtea.verborum.core.extensions.json
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

/**
 * Guards the deserialisation of payloads captured verbatim from the running backend, so a rename
 * on either side fails here rather than silently falling back to "created just now".
 */
@OptIn(ExperimentalSerializationApi::class)
class ApiTimestampWireTest {

    private val originalTimeZone: TimeZone = TimeZone.getDefault()

    @Before
    fun pinTimeZone() {
        // The server emits no offset, so the value is read in the device's zone.
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `dictionary response carries the server creation timestamp`() {
        val payload = """
            {
              "dictionaryId": "6dc96c78-6de5-435c-a73a-3fea3066edf7",
              "userId": "00000000-0000-0000-0000-000000000000",
              "name": "__ts_wire__",
              "isPublic": false,
              "fromLang": "de",
              "toLang": "en",
              "createdAt": "2026-07-19T19:27:20.400123Z",
              "updatedAt": "2026-07-19T19:27:20.401999Z"
            }
        """.trimIndent()

        val dictionary = json.decodeFromString<DictionaryResponse>(payload)
            .convertToDictionary(fallbackCreatedAt = FALLBACK, fallbackUpdatedAt = FALLBACK)

        // Microseconds are truncated to milliseconds; the trailing Z is read as UTC.
        assertEquals(1_784_489_240_400L, dictionary.createdAt)
        assertEquals(1_784_489_240_401L, dictionary.updatedAt)
    }

    @Test
    fun `word response carries the server timestamps and level`() {
        val payload = """
            {
              "wordId": "cc380c9d-2824-4c15-8734-6fad68f555c9",
              "dictionaryId": "f141990b-56f6-4a73-83fd-d0c4e84f92e6",
              "word": "[\"a\"]",
              "wordMeta": "{}",
              "translation": "[\"b\"]",
              "translationMeta": "{}",
              "level": 5,
              "createdAt": "2026-07-19T19:27:20.400123Z",
              "updatedAt": "2026-07-19T19:27:20.400123Z"
            }
        """.trimIndent()

        val word = json.decodeFromString<WordResponse>(payload)
            .convertToWord(
                dictionaryId = "f141990b-56f6-4a73-83fd-d0c4e84f92e6",
                fallbackCreatedAt = FALLBACK,
                fallbackUpdatedAt = FALLBACK,
            )

        assertEquals(1_784_489_240_400L, word.createdAt)
        assertEquals(1_784_489_240_400L, word.updatedAt)
        // A valid server level is adopted, and the row is considered in sync.
        assertEquals(5, word.level)
        assertTrue(word.isSynced)
    }

    @Test
    fun `a backend that omits the timestamps falls back instead of failing`() {
        val payload = """
            {
              "dictionaryId": "d-1",
              "userId": "u-1",
              "name": "Legacy",
              "isPublic": false,
              "fromLang": "de",
              "toLang": "en"
            }
        """.trimIndent()

        val dictionary = json.decodeFromString<DictionaryResponse>(payload)
            .convertToDictionary(fallbackCreatedAt = FALLBACK, fallbackUpdatedAt = FALLBACK)

        assertEquals(FALLBACK, dictionary.createdAt)
        assertEquals(FALLBACK, dictionary.updatedAt)
    }

    private companion object {
        const val FALLBACK = 1_700_000_000_000L
    }
}
