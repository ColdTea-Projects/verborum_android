package de.coldtea.verborum.bibliotheca.word.ui.createword

import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.testDictionaryUi
import de.coldtea.verborum.bibliotheca.testWordUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.CreateWordState
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.Gender
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CreateWordViewModelTest : BaseTest() {

    @MockK
    private lateinit var dictionaryService: DictionaryService

    @MockK
    private lateinit var wordService: WordService

    private lateinit var viewModel: CreateWordViewModel

    override fun setUp() {
        super.setUp()
        viewModel = CreateWordViewModel(dictionaryService, wordService)
    }

    // region initial state

    @Test
    fun `initial createWordState is Loading`() = runTest {
        assertEquals(CreateWordState.Loading, viewModel.createWordState.first())
    }

    // endregion

    // region init

    @Test
    fun `init emits Success with the observed dictionary`() = runTest {
        val dictionaryId = "dict-1"
        val dictionary = testDictionaryUi(dictionaryId = dictionaryId, name = "German Basics")
        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(dictionary)

        viewModel.init(dictionaryId)

        assertEquals(CreateWordState.Success(dictionary), viewModel.createWordState.first())
    }

    @Test
    fun `init with a wordId loads the word into the state for edit mode`() = runTest {
        val dictionary = testDictionaryUi(dictionaryId = "dict-1")
        val word = testWordUi(wordId = "word-1", dictionaryId = "dict-1")
        every { dictionaryService.observeDictionary("dict-1") } returns flowOf(dictionary)
        coEvery { wordService.getWord("word-1") } returns word

        viewModel.init("dict-1", "word-1")

        assertEquals(
            CreateWordState.Success(dictionary, editingWord = word),
            viewModel.createWordState.first(),
        )
    }

    @Test
    fun `init emits Failed when dictionary flow throws`() = runTest {
        val dictionaryId = "dict-1"
        every { dictionaryService.observeDictionary(dictionaryId) } returns flow {
            throw RuntimeException("db error")
        }

        viewModel.init(dictionaryId)

        assertEquals(CreateWordState.Failed, viewModel.createWordState.first())
    }

    // endregion

    // region saveWord

    @Test
    fun `saveWord composes word from dictionary and inputs and delegates to WordService`() = runTest {
        val dictionary = testDictionaryUi(dictionaryId = "dict-1", fromLang = "de", toLang = "en")
        every { dictionaryService.observeDictionary("dict-1") } returns flowOf(dictionary)
        viewModel.init("dict-1")

        val wordSlot = slot<Word>()

        viewModel.saveWord(
            wordType = WordType.NOUN,
            sourceInput = WordFormInput(
                text = " Haus ",
                gender = Gender.NEUTER,
                fields = mapOf(FieldKey.PLURAL to "Häuser"),
            ),
            targetInput = WordFormInput(text = "house", fields = mapOf(FieldKey.PLURAL to "houses")),
        )

        coVerify(exactly = 1) { wordService.saveWord(capture(wordSlot)) }
        val saved = wordSlot.captured
        assertEquals("", saved.wordId)
        assertEquals("dict-1", saved.dictionaryId)
        assertEquals("das Haus", saved.word)
        assertEquals("{de;type=noun;gender=n;plural=Häuser}", saved.wordMeta)
        assertEquals("house", saved.translation)
        assertEquals("{en;type=noun;plural=houses}", saved.translationMeta)
        assertEquals(0, saved.level)
        assertFalse(saved.isSynced)
    }

    @Test
    fun `saveWord for free text composes bare language meta without article`() = runTest {
        val dictionary = testDictionaryUi(dictionaryId = "dict-1", fromLang = "de", toLang = "en")
        every { dictionaryService.observeDictionary("dict-1") } returns flowOf(dictionary)
        viewModel.init("dict-1")

        val wordSlot = slot<Word>()

        viewModel.saveWord(
            wordType = WordType.FREE_TEXT,
            sourceInput = WordFormInput(text = "Wie geht es dir?"),
            targetInput = WordFormInput(text = "How are you?"),
        )

        coVerify(exactly = 1) { wordService.saveWord(capture(wordSlot)) }
        val saved = wordSlot.captured
        assertEquals("Wie geht es dir?", saved.word)
        assertEquals("{de}", saved.wordMeta)
        assertEquals("How are you?", saved.translation)
        assertEquals("{en}", saved.translationMeta)
    }

    @Test
    fun `saveWord in edit mode keeps the wordId level and createdAt of the edited word`() = runTest {
        val dictionary = testDictionaryUi(dictionaryId = "dict-1", fromLang = "de", toLang = "en")
        val editedWord = testWordUi(
            wordId = "word-1",
            dictionaryId = "dict-1",
            level = 4,
            createdAt = 1_000L,
        )
        every { dictionaryService.observeDictionary("dict-1") } returns flowOf(dictionary)
        coEvery { wordService.getWord("word-1") } returns editedWord
        viewModel.init("dict-1", "word-1")

        val wordSlot = slot<Word>()

        viewModel.saveWord(
            wordType = WordType.NOUN,
            sourceInput = WordFormInput(text = "Haus", gender = Gender.NEUTER),
            targetInput = WordFormInput(text = "house"),
        )

        coVerify(exactly = 1) { wordService.saveWord(capture(wordSlot)) }
        val saved = wordSlot.captured
        assertEquals("word-1", saved.wordId)
        assertEquals("das Haus", saved.word)
        assertEquals(4, saved.level)
        assertEquals(1_000L, saved.createdAt)
        assertFalse(saved.isSynced)
    }

    @Test
    fun `saveWord does nothing when state is not Success`() = runTest {
        // ViewModel is still in Loading state — init() was never called.
        viewModel.saveWord(
            wordType = WordType.NOUN,
            sourceInput = WordFormInput(text = "Haus"),
            targetInput = WordFormInput(text = "house"),
        )

        coVerify(exactly = 0) { wordService.saveWord(any()) }
    }

    // endregion
}
