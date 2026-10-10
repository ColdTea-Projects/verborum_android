package de.coldtea.verborum.bibliotheca.imported.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.testImportedDictionary
import de.coldtea.verborum.bibliotheca.testImportedWord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** [DaoImportedWord] against a real in-memory Room database: progress survives a refresh. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DaoImportedWordTest {

    private lateinit var db: BibliothecaDatabase
    private lateinit var dao: DaoImportedWord

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BibliothecaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.daoImportedWord
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `replaceWords keeps local levels, drops removed words and adds new ones at 0`() = runTest {
        dao.insertAll(
            listOf(
                testImportedWord(wordId = "kept", level = 5).convertToEntity(),
                testImportedWord(wordId = "removed", level = 3).convertToEntity(),
            )
        )

        dao.replaceWords(
            "imported-id",
            listOf(
                testImportedWord(wordId = "kept", level = 0).convertToEntity(),
                testImportedWord(wordId = "new", level = 0).convertToEntity(),
            ),
        )

        val levels = dao.getByDictionary("imported-id").associate { it.wordId to it.level }
        assertEquals(mapOf("kept" to 5, "new" to 0), levels)
    }

    @Test
    fun `observeInLanguagePair matches the downloaded dictionaries of that direction, in any case`() = runTest {
        db.daoImportedDictionary.insert(testImportedDictionary(dictionaryId = "a", fromLang = "PL", toLang = "UK").convertToEntity())
        db.daoImportedDictionary.insert(testImportedDictionary(dictionaryId = "b", fromLang = "UK", toLang = "PL").convertToEntity())
        dao.insert(testImportedWord(wordId = "wa", dictionaryId = "a").convertToEntity())
        dao.insert(testImportedWord(wordId = "wb", dictionaryId = "b").convertToEntity())

        val ids = dao.observeInLanguagePair("pl", "uk").first().map { it.wordId }

        assertEquals(listOf("wa"), ids)
    }
}
