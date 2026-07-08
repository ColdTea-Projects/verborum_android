package de.coldtea.verborum.bibliotheca.word.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.testWordUi
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordByDictionaryIdApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordByDictionaryIdUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.SaveWordUseCase
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.core.BaseTest
import io.mockk.impl.annotations.MockK
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WordServiceTest : BaseTest() {

    @MockK
    private lateinit var observeWordsByDictionaryUseCase: ObserveWordsByDictionaryUseCase

    // invoke returns Response<Unit> — not covered by relaxUnitFun, so relax fully for verify-only use.
    @MockK(relaxed = true)
    private lateinit var deleteWordByDictionaryIdApiUseCase: DeleteWordByDictionaryIdApiUseCase

    // invoke returns the repository result (non-Unit) — relax fully for verify-only use.
    @MockK(relaxed = true)
    private lateinit var deleteWordByDictionaryIdUseCase: DeleteWordByDictionaryIdUseCase

    @MockK
    private lateinit var saveWordUseCase: SaveWordUseCase

    @MockK
    private lateinit var syncService: SyncService

    @MockK
    private lateinit var uploadService: UploadService

    private lateinit var wordService: WordService

    override fun setUp() {
        super.setUp()
        wordService = WordService(
            observeWordsByDictionaryUseCase = observeWordsByDictionaryUseCase,
            deleteWordByDictionaryIdApiUseCase = deleteWordByDictionaryIdApiUseCase,
            deleteWordByDictionaryIdUseCase = deleteWordByDictionaryIdUseCase,
            saveWordUseCase = saveWordUseCase,
            syncService = syncService,
            uploadService = uploadService,
        )
    }

    // region observeWordsByDictionary

    @Test
    fun `observeWordsByDictionary maps domain Word to WordUi`() = runTest {
        val dictionaryId = "dict-1"
        val word = testWord(wordId = "w-1", dictionaryId = dictionaryId)
        every { observeWordsByDictionaryUseCase.invoke(dictionaryId) } returns flowOf(listOf(word))

        val result = wordService.observeWordsByDictionary(dictionaryId).first()

        assertEquals(listOf(word.convertToUi()), result)
    }

    @Test
    fun `observeWordsByDictionary maps multiple words preserving order`() = runTest {
        val dictionaryId = "dict-1"
        val words = listOf(
            testWord(wordId = "w-1", word = "apple"),
            testWord(wordId = "w-2", word = "bread"),
            testWord(wordId = "w-3", word = "coffee"),
        )
        every { observeWordsByDictionaryUseCase.invoke(dictionaryId) } returns flowOf(words)

        val result = wordService.observeWordsByDictionary(dictionaryId).first()

        assertEquals(words.map(Word::convertToUi), result)
    }

    @Test
    fun `observeWordsByDictionary suppresses duplicate emissions`() = runTest {
        val dictionaryId = "dict-1"
        val words = listOf(testWord(wordId = "w-1"))
        // emit the exact same list twice — only one downstream emission expected
        every { observeWordsByDictionaryUseCase.invoke(dictionaryId) } returns flow {
            emit(words)
            emit(words)
        }

        val emissions = wordService.observeWordsByDictionary(dictionaryId).toList()

        assertEquals(1, emissions.size)
    }

    @Test
    fun `observeWordsByDictionary emits both events when list content changes`() = runTest {
        val dictionaryId = "dict-1"
        val first = listOf(testWord(wordId = "w-1", level = 0))
        val second = listOf(testWord(wordId = "w-1", level = 1))
        every { observeWordsByDictionaryUseCase.invoke(dictionaryId) } returns flow {
            emit(first)
            emit(second)
        }

        val emissions = wordService.observeWordsByDictionary(dictionaryId).toList()

        assertEquals(2, emissions.size)
        assertEquals(0, emissions[0].first().level)
        assertEquals(1, emissions[1].first().level)
    }

    // endregion

    // region saveWord

    @Test
    fun `saveWord delegates to SaveWordUseCase`() = runTest {
        val word = testWord()

        wordService.saveWord(word)

        coVerify(exactly = 1) { saveWordUseCase.invoke(word) }
    }

    // endregion

    // region cleanWordsInDictionary

    @Test
    fun `cleanWordsInDictionary calls api delete use case`() = runTest {
        val dictionaryId = "dict-1"

        wordService.cleanWordsInDictionary(dictionaryId)

        coVerify(exactly = 1) { deleteWordByDictionaryIdApiUseCase.invoke(dictionaryId) }
    }

    @Test
    fun `cleanWordsInDictionary calls local delete use case`() = runTest {
        val dictionaryId = "dict-1"

        wordService.cleanWordsInDictionary(dictionaryId)

        coVerify(exactly = 1) { deleteWordByDictionaryIdUseCase.invoke(dictionaryId) }
    }

    // endregion
}
