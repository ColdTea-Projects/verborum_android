package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import de.coldtea.verborum.core.BaseTest
import io.mockk.impl.annotations.MockK
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveWordUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: SaveWordUseCase

    override fun setUp() {
        super.setUp()
        useCase = SaveWordUseCase(wordRepository)
    }

    // region new word (blank wordId)

    @Test
    fun `invoke with blank wordId calls saveWord not updateWord`() = runTest {
        val word = testWord(wordId = "")
        coEvery { wordRepository.saveWord(any()) } returns 1L

        useCase.invoke(word)

        coVerify(exactly = 1) { wordRepository.saveWord(any()) }
        coVerify(exactly = 0) { wordRepository.updateWord(any()) }
    }

    @Test
    fun `invoke with blank wordId generates a non-blank UUID`() = runTest {
        val word = testWord(wordId = "")
        val savedSlot = slot<WordEntity>()
        coEvery { wordRepository.saveWord(capture(savedSlot)) } returns 1L

        useCase.invoke(word)

        assertTrue(savedSlot.captured.wordId.isNotBlank())
    }

    @Test
    fun `invoke with blank wordId sets isSynced to false regardless of input`() = runTest {
        val word = testWord(wordId = "", isSynced = true)
        val savedSlot = slot<WordEntity>()
        coEvery { wordRepository.saveWord(capture(savedSlot)) } returns 1L

        useCase.invoke(word)

        assertFalse(savedSlot.captured.isSynced)
    }

    @Test
    fun `invoke with blank wordId preserves all other word fields`() = runTest {
        val word = testWord(
            wordId = "",
            dictionaryId = "dict-42",
            word = "bread",
            wordMeta = "{en}",
            translation = "das Brot",
            translationMeta = "{de}",
            level = 3,
        )
        val savedSlot = slot<WordEntity>()
        coEvery { wordRepository.saveWord(capture(savedSlot)) } returns 1L

        useCase.invoke(word)

        with(savedSlot.captured) {
            assertEquals("dict-42", dictionaryId)
            assertEquals("bread", this.word)
            assertEquals("{en}", wordMeta)
            assertEquals("das Brot", translation)
            assertEquals("{de}", translationMeta)
            assertEquals(3, level)
        }
    }

    // endregion

    // region existing word (non-blank wordId)

    @Test
    fun `invoke with existing wordId calls updateWord not saveWord`() = runTest {
        val word = testWord(wordId = "word-123")

        useCase.invoke(word)

        coVerify(exactly = 1) { wordRepository.updateWord(any()) }
        coVerify(exactly = 0) { wordRepository.saveWord(any()) }
    }

    @Test
    fun `invoke with existing wordId preserves the original wordId`() = runTest {
        val originalId = "word-existing-42"
        val word = testWord(wordId = originalId)
        val updatedSlot = slot<WordEntity>()
        coEvery { wordRepository.updateWord(capture(updatedSlot)) } returns Unit

        useCase.invoke(word)

        assertEquals(originalId, updatedSlot.captured.wordId)
    }

    @Test
    fun `invoke with existing wordId sets isSynced to false regardless of input`() = runTest {
        val word = testWord(wordId = "word-123", isSynced = true)
        val updatedSlot = slot<WordEntity>()
        coEvery { wordRepository.updateWord(capture(updatedSlot)) } returns Unit

        useCase.invoke(word)

        assertFalse(updatedSlot.captured.isSynced)
    }

    // endregion
}
