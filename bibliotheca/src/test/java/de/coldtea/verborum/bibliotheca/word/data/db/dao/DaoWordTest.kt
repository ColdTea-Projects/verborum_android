package de.coldtea.verborum.bibliotheca.word.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.testDictionaryEntity
import de.coldtea.verborum.bibliotheca.testWordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration tests for [DaoWord] against a real (in-memory) Room database via Robolectric —
 * tombstone filters, the language-pair distractor join, per-dictionary counts, and the
 * guest-migration "mark unsynced by owner" update.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DaoWordTest {

    private lateinit var db: BibliothecaDatabase
    private lateinit var dao: DaoWord

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BibliothecaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.daoWord
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `observeWordsByDictionary returns live words for the dictionary, excluding tombstones`() =
        runTest {
            dao.insert(testWordEntity(wordId = "w1", dictionaryId = "d1"))
            dao.insert(testWordEntity(wordId = "w2", dictionaryId = "d1", isDeleted = true))
            dao.insert(testWordEntity(wordId = "w3", dictionaryId = "d2"))

            val ids = dao.observeWordsByDictionary("d1").first().map { it.wordId }

            assertEquals(listOf("w1"), ids)
        }

    @Test
    fun `getWordsByDictionary returns all words including tombstones`() = runTest {
        dao.insert(testWordEntity(wordId = "w1", dictionaryId = "d1"))
        dao.insert(testWordEntity(wordId = "w2", dictionaryId = "d1", isDeleted = true))

        assertEquals(2, dao.getWordsByDictionary("d1").size)
    }

    @Test
    fun `observeWordsInLanguagePairOf draws from every dictionary with the same pair`() = runTest {
        // Two en→de dictionaries and one en→fr; the pair pool must span both en→de, not en→fr.
        db.daoDictionary.insert(testDictionaryEntity(dictionaryId = "d1", fromLang = "en", toLang = "de"))
        db.daoDictionary.insert(testDictionaryEntity(dictionaryId = "d2", fromLang = "en", toLang = "de"))
        db.daoDictionary.insert(testDictionaryEntity(dictionaryId = "d3", fromLang = "en", toLang = "fr"))
        dao.insert(testWordEntity(wordId = "w1", dictionaryId = "d1"))
        dao.insert(testWordEntity(wordId = "w2", dictionaryId = "d2"))
        dao.insert(testWordEntity(wordId = "w3", dictionaryId = "d3"))
        dao.insert(testWordEntity(wordId = "w4", dictionaryId = "d2", isDeleted = true))

        val ids = dao.observeWordsInLanguagePairOf("d1").first().map { it.wordId }.sorted()

        assertEquals(listOf("w1", "w2"), ids)
    }

    @Test
    fun `observeWordCounts groups live words per dictionary`() = runTest {
        dao.insert(testWordEntity(wordId = "w1", dictionaryId = "d1"))
        dao.insert(testWordEntity(wordId = "w2", dictionaryId = "d1"))
        dao.insert(testWordEntity(wordId = "w3", dictionaryId = "d1", isDeleted = true))
        dao.insert(testWordEntity(wordId = "w4", dictionaryId = "d2"))

        val counts = dao.observeWordCounts().first().associate { it.dictionaryId to it.count }

        assertEquals(2, counts["d1"])
        assertEquals(1, counts["d2"])
    }

    @Test
    fun `markWordDeleted flags the tombstone`() = runTest {
        dao.insert(testWordEntity(wordId = "w1", dictionaryId = "d1"))

        dao.markWordDeleted("w1")

        assertTrue(dao.getWord("w1").isDeleted)
    }

    @Test
    fun `markWordsUnsyncedInDictionaries resets isSynced only for the named dictionaries`() =
        runTest {
            // The migrated dictionary and one the user already owned — only the former's words
            // may be re-flagged, or the whole corpus would re-upload.
            db.daoDictionary.insert(testDictionaryEntity(dictionaryId = "migrated", userId = "me"))
            db.daoDictionary.insert(testDictionaryEntity(dictionaryId = "existing", userId = "me"))
            dao.insert(testWordEntity(wordId = "w1", dictionaryId = "migrated", isSynced = true))
            dao.insert(testWordEntity(wordId = "w2", dictionaryId = "existing", isSynced = true))

            dao.markWordsUnsyncedInDictionaries(listOf("migrated"))

            assertFalse(dao.getWord("w1").isSynced)
            assertTrue(dao.getWord("w2").isSynced)
        }

    @Test
    fun `updateWordLevel writes the level and flags the word for upload`() = runTest {
        dao.insert(
            testWordEntity(wordId = "w1", dictionaryId = "d1", level = 2, isSynced = true)
        )

        dao.updateWordLevel("w1", level = 3, updatedAt = 500L)

        val stored = dao.getWord("w1")
        assertEquals(3, stored.level)
        assertEquals(500L, stored.updatedAt)
        assertFalse(stored.isSynced)
    }

    @Test
    fun `updateWordLevel never resurrects a tombstoned word`() = runTest {
        // A practice session started before the word was deleted still holds it in its snapshot;
        // answering it must not clear the tombstone or revive the row.
        dao.insert(
            testWordEntity(wordId = "w1", dictionaryId = "d1", level = 2, isDeleted = true)
        )

        dao.updateWordLevel("w1", level = 3, updatedAt = 500L)

        val stored = dao.getWord("w1")
        assertTrue(stored.isDeleted)
        assertEquals(2, stored.level)
    }

    @Test
    fun `markWordSynced flips only the flag for the version that was uploaded`() = runTest {
        dao.insert(
            testWordEntity(
                wordId = "w1",
                dictionaryId = "d1",
                word = "uploaded",
                isSynced = false,
                updatedAt = 100L,
            )
        )

        dao.markWordSynced("w1", updatedAt = 100L)

        val stored = dao.getWord("w1")
        assertTrue(stored.isSynced)
        assertEquals("uploaded", stored.word)
    }

    @Test
    fun `markWordSynced leaves a word edited mid-upload unsynced`() = runTest {
        // The upload snapshotted updated_at = 100, but the user edited the row while the request
        // was in flight — the edit must survive and stay queued for the next sync.
        dao.insert(
            testWordEntity(
                wordId = "w1",
                dictionaryId = "d1",
                word = "edited during upload",
                isSynced = false,
                updatedAt = 200L,
            )
        )

        dao.markWordSynced("w1", updatedAt = 100L)

        val stored = dao.getWord("w1")
        assertFalse(stored.isSynced)
        assertEquals("edited during upload", stored.word)
    }

    @Test
    fun `deleteWordsByDictionary removes every word of the dictionary`() = runTest {
        dao.insert(testWordEntity(wordId = "w1", dictionaryId = "d1"))
        dao.insert(testWordEntity(wordId = "w2", dictionaryId = "d1"))
        dao.insert(testWordEntity(wordId = "w3", dictionaryId = "d2"))

        dao.deleteWordsByDictionary("d1")

        assertTrue(dao.getWordsByDictionary("d1").isEmpty())
        assertEquals(1, dao.getWordsByDictionary("d2").size)
    }

    @Test
    fun `observePendingUploadCount counts unsynced or tombstoned words`() = runTest {
        dao.insert(testWordEntity(wordId = "w1", isSynced = true, isDeleted = false))
        dao.insert(testWordEntity(wordId = "w2", isSynced = false))
        dao.insert(testWordEntity(wordId = "w3", isSynced = true, isDeleted = true))

        assertEquals(2, dao.observePendingUploadCount().first())
    }
}
