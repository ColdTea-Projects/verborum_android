package de.coldtea.verborum.bibliotheca.word.data

import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.testWordEntity
import de.coldtea.verborum.bibliotheca.word.data.db.dao.DaoWord
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import de.coldtea.verborum.core.BaseTest
import io.mockk.impl.annotations.MockK
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WordRepositoryTest : BaseTest() {

    @MockK
    private lateinit var bibliothecaDatabase: BibliothecaDatabase

    // Relaxed so that Long-returning insert() and Unit-returning update/delete()
    // don't require explicit stubbing in tests that only verify delegation.
    @RelaxedMockK
    private lateinit var daoWord: DaoWord

    private lateinit var repository: WordRepository

    override fun setUp() {
        super.setUp()
        every { bibliothecaDatabase.daoWord } returns daoWord
        repository = WordRepository(bibliothecaDatabase)
    }

    // region getWordsByDictionary

    @Test
    fun `getWordsByDictionary returns list from dao`() = runTest {
        val dictionaryId = "dict-1"
        val expected = listOf(testWordEntity(dictionaryId = dictionaryId))
        coEvery { daoWord.getWordsByDictionary(dictionaryId) } returns expected

        val result = repository.getWordsByDictionary(dictionaryId)

        assertEquals(expected, result)
    }

    @Test
    fun `getWordsByDictionary returns empty list when dao returns empty`() = runTest {
        val dictionaryId = "dict-empty"
        coEvery { daoWord.getWordsByDictionary(dictionaryId) } returns emptyList()

        val result = repository.getWordsByDictionary(dictionaryId)

        assertEquals(emptyList<WordEntity>(), result)
    }

    // endregion

    // region observeWordsByDictionary

    @Test
    fun `observeWordsByDictionary emits list from dao`() = runTest {
        val dictionaryId = "dict-1"
        val entities = listOf(testWordEntity(dictionaryId = dictionaryId))
        every { daoWord.observeWordsByDictionary(dictionaryId) } returns flowOf(entities)

        val result = repository.observeWordsByDictionary(dictionaryId).first()

        assertEquals(entities, result)
    }

    // endregion

    // region getWord

    @Test
    fun `getWord returns entity from dao`() = runTest {
        val wordId = "word-1"
        val entity = testWordEntity(wordId = wordId)
        coEvery { daoWord.getWord(wordId) } returns entity

        val result = repository.getWord(wordId)

        assertEquals(entity, result)
    }

    // endregion

    // region saveWord

    @Test
    fun `saveWord delegates to dao insert`() = runTest {
        val entity = testWordEntity()

        repository.saveWord(entity)

        coVerify(exactly = 1) { daoWord.insert(entity) }
    }

    // endregion

    // region updateWord

    @Test
    fun `updateWord delegates to dao update`() = runTest {
        val entity = testWordEntity()

        repository.updateWord(entity)

        coVerify(exactly = 1) { daoWord.update(entity) }
    }

    // endregion

    // region deleteWords

    @Test
    fun `deleteWords delegates to dao with correct ids`() = runTest {
        val wordIds = listOf("w-1", "w-2", "w-3")

        repository.deleteWords(wordIds)

        coVerify(exactly = 1) { daoWord.deleteWords(wordIds) }
    }

    // endregion

    // region deleteWordsByDictionary

    @Test
    fun `deleteWordsByDictionary delegates to dao with correct dictionaryId`() = runTest {
        val dictionaryId = "dict-1"

        repository.deleteWordsByDictionary(dictionaryId)

        coVerify(exactly = 1) { daoWord.deleteWordsByDictionary(dictionaryId) }
    }

    // endregion
}
