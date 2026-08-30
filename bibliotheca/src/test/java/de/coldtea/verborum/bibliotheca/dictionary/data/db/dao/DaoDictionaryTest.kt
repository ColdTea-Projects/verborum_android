package de.coldtea.verborum.bibliotheca.dictionary.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.testDictionaryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration tests for [DaoDictionary] against a real (in-memory) Room database via Robolectric.
 * These cover the query logic — ordering, tombstone filters, owner reassignment, pending-upload
 * counting, and the tags JSON round-trip — that the mock-backed repository unit tests can't reach.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DaoDictionaryTest {

    private lateinit var db: BibliothecaDatabase
    private lateinit var dao: DaoDictionary

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BibliothecaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.daoDictionary
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `observeAllDictionaries orders newest first then by id, excluding tombstones`() = runTest {
        dao.insert(testDictionaryEntity(dictionaryId = "b", createdAt = 100L))
        dao.insert(testDictionaryEntity(dictionaryId = "a", createdAt = 200L))
        dao.insert(testDictionaryEntity(dictionaryId = "c", createdAt = 200L))
        dao.insert(testDictionaryEntity(dictionaryId = "d", createdAt = 300L, isDeleted = true))

        val ids = dao.observeAllDictionaries().first().map { it.dictionaryId }

        // 200(a,c ordered by id asc) then 100(b); the tombstoned 300 is hidden.
        assertEquals(listOf("a", "c", "b"), ids)
    }

    @Test
    fun `getAllDictionaries returns every row including tombstones`() = runTest {
        dao.insert(testDictionaryEntity(dictionaryId = "1"))
        dao.insert(testDictionaryEntity(dictionaryId = "2", isDeleted = true))

        assertEquals(2, dao.getAllDictionaries().size)
    }

    @Test
    fun `markDictionaryDeleted flags the tombstone and hides it from observation`() = runTest {
        dao.insert(testDictionaryEntity(dictionaryId = "1"))

        dao.markDictionaryDeleted("1")

        assertTrue(dao.getDictionary("1").isDeleted)
        assertTrue(dao.observeAllDictionaries().first().isEmpty())
    }

    @Test
    fun `markDictionarySynced flips only the flag for the version that was uploaded`() = runTest {
        dao.insert(
            testDictionaryEntity(
                dictionaryId = "1",
                name = "uploaded",
                isSynced = false,
                updatedAt = 100L,
            )
        )

        dao.markDictionarySynced("1", updatedAt = 100L)

        val stored = dao.getDictionary("1")
        assertTrue(stored.isSynced)
        assertEquals("uploaded", stored.name)
    }

    @Test
    fun `markDictionarySynced leaves a dictionary edited mid-upload unsynced`() = runTest {
        // The upload snapshotted updated_at = 100; the rename landed while the request was in
        // flight, so the new name must survive and stay queued for the next sync.
        dao.insert(
            testDictionaryEntity(
                dictionaryId = "1",
                name = "renamed during upload",
                isSynced = false,
                updatedAt = 200L,
            )
        )

        dao.markDictionarySynced("1", updatedAt = 100L)

        val stored = dao.getDictionary("1")
        assertFalse(stored.isSynced)
        assertEquals("renamed during upload", stored.name)
    }

    @Test
    fun `reassignOwner rewrites the owner, resets isSynced, and returns the affected count`() =
        runTest {
            dao.insert(testDictionaryEntity(dictionaryId = "1", userId = "guest", isSynced = true))
            dao.insert(testDictionaryEntity(dictionaryId = "2", userId = "guest", isSynced = true))
            dao.insert(testDictionaryEntity(dictionaryId = "3", userId = "other", isSynced = true))

            val affected = dao.reassignOwner("guest", "sub-1")

            assertEquals(2, affected)
            assertEquals("sub-1", dao.getDictionary("1").userId)
            assertFalse(dao.getDictionary("1").isSynced)
            // Rows owned by someone else are untouched.
            assertEquals("other", dao.getDictionary("3").userId)
            assertTrue(dao.getDictionary("3").isSynced)
        }

    @Test
    fun `getDictionariesByUser returns only that owner's rows`() = runTest {
        dao.insert(testDictionaryEntity(dictionaryId = "1", userId = "me"))
        dao.insert(testDictionaryEntity(dictionaryId = "2", userId = "me"))
        dao.insert(testDictionaryEntity(dictionaryId = "3", userId = "you"))

        assertEquals(2, dao.getDictionariesByUser("me").size)
        assertEquals("3", dao.getDictionariesByUser("you").single().dictionaryId)
    }

    @Test
    fun `observePendingUploadCount counts unsynced or tombstoned rows`() = runTest {
        dao.insert(testDictionaryEntity(dictionaryId = "1", isSynced = true, isDeleted = false))
        dao.insert(testDictionaryEntity(dictionaryId = "2", isSynced = false))
        dao.insert(testDictionaryEntity(dictionaryId = "3", isSynced = true, isDeleted = true))

        assertEquals(2, dao.observePendingUploadCount().first())
    }

    @Test
    fun `deleteDictionary hard-removes the row`() = runTest {
        dao.insert(testDictionaryEntity(dictionaryId = "1"))

        dao.deleteDictionary("1")

        assertNull(dao.getAllDictionaries().firstOrNull { it.dictionaryId == "1" })
    }

    @Test
    fun `tags JSON round-trips through insert, read, and the domain converter`() = runTest {
        dao.insert(testDictionaryEntity(dictionaryId = "1", tags = """["food_drink","a1"]"""))

        val entity = dao.getDictionary("1")
        assertEquals("""["food_drink","a1"]""", entity.tags)
        assertEquals(listOf("food_drink", "a1"), entity.convertToDictionary().tags)
    }

    @Test
    fun `a dictionary with no tags decodes to an empty list`() = runTest {
        dao.insert(testDictionaryEntity(dictionaryId = "1"))

        assertEquals(emptyList<String>(), dao.getDictionary("1").convertToDictionary().tags)
    }
}
