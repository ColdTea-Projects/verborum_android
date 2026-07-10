package de.coldtea.verborum.bibliotheca.word.domain.usecase.api

import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.bibliotheca.word.data.api.model.WordBundleRequest
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveWordApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordApi: WordApi

    private lateinit var useCase: SaveWordApiUseCase

    override fun setUp() {
        super.setUp()
        useCase = SaveWordApiUseCase(wordApi)
    }

    // region new word (blank wordId)

    @Test
    fun `invoke with blank wordId calls createWords not updateWords`() = runTest {
        coEvery { wordApi.createWords(any()) } returns mockk()

        useCase.invoke(testWord(wordId = ""))

        coVerify(exactly = 1) { wordApi.createWords(any()) }
        coVerify(exactly = 0) { wordApi.updateWords(any()) }
    }

    @Test
    fun `invoke with blank wordId sends a generated non-blank wordId`() = runTest {
        val bundlesSlot = slot<List<WordBundleRequest>>()
        coEvery { wordApi.createWords(capture(bundlesSlot)) } returns mockk()

        useCase.invoke(testWord(wordId = ""))

        assertTrue(bundlesSlot.captured.single().words.single().wordId.isNotBlank())
    }

    @Test
    fun `invoke with blank wordId bundles the converted word under its dictionaryId`() = runTest {
        val word = testWord(
            wordId = "",
            dictionaryId = "dict-42",
            word = "bread",
            wordMeta = "{en}",
            translation = "das Brot",
            translationMeta = "{de}",
        )
        val bundlesSlot = slot<List<WordBundleRequest>>()
        coEvery { wordApi.createWords(capture(bundlesSlot)) } returns mockk()

        useCase.invoke(word)

        val bundle = bundlesSlot.captured.single()
        assertEquals("dict-42", bundle.dictionaryId)
        with(bundle.words.single()) {
            assertEquals("dict-42", dictionaryId)
            assertEquals("bread", this.word)
            assertEquals("{en}", wordMeta)
            assertEquals("das Brot", translation)
            assertEquals("{de}", translationMeta)
        }
    }

    // endregion

    // region existing word (non-blank wordId)

    @Test
    fun `invoke with existing wordId calls updateWords not createWords`() = runTest {
        coEvery { wordApi.updateWords(any()) } returns mockk()

        useCase.invoke(testWord(wordId = "word-123"))

        coVerify(exactly = 1) { wordApi.updateWords(any()) }
        coVerify(exactly = 0) { wordApi.createWords(any()) }
    }

    @Test
    fun `invoke with existing wordId preserves the original wordId in the request`() = runTest {
        val bundlesSlot = slot<List<WordBundleRequest>>()
        coEvery { wordApi.updateWords(capture(bundlesSlot)) } returns mockk()

        useCase.invoke(testWord(wordId = "word-existing-42", dictionaryId = "dict-42"))

        val bundle = bundlesSlot.captured.single()
        assertEquals("dict-42", bundle.dictionaryId)
        assertEquals("word-existing-42", bundle.words.single().wordId)
    }

    // endregion
}
