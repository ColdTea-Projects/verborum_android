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
        // Every state emission now combines the dictionary with the live word counts; individual
        // tests override this when the count is what they are about.
        every { wordService.observeWordCounts() } returns flowOf(emptyMap())
        viewModel = CreateWordViewModel(dictionaryService, wordService)
    }

    // region initial state

    @Test
    fun `initial createWordState is Loading`() = runTest {
        assertEquals(CreateWordState.Loading, viewModel.createWordState.first())
    }

    // endregion

    // region word count
    @Test
    fun `the observed word count rides along on the dictionary`() = runTest {
        val dictionary = testDictionaryUi(dictionaryId = "dict-1")
        every { dictionaryService.observeDictionary("dict-1") } returns flowOf(dictionary)
        every { wordService.observeWordCounts() } returns flowOf(mapOf("dict-1" to 9))

        viewModel.init("dict-1")

        val state = viewModel.createWordState.first() as CreateWordState.Success
        assertEquals(9, state.dictionaryUi.wordCount)
    }

    @Test
    fun `a dictionary with no words counts zero rather than going missing`() = runTest {
        val dictionary = testDictionaryUi(dictionaryId = "dict-1")
        every { dictionaryService.observeDictionary("dict-1") } returns flowOf(dictionary)
        every { wordService.observeWordCounts() } returns flowOf(mapOf("other-dict" to 4))

        viewModel.init("dict-1")

        val state = viewModel.createWordState.first() as CreateWordState.Success
        assertEquals(0, state.dictionaryUi.wordCount)
    }

    @Test
    fun `the edited word is read once, not again on every count change`() = runTest {
        val dictionary = testDictionaryUi(dictionaryId = "dict-1")
        val word = testWordUi(wordId = "word-1", dictionaryId = "dict-1")
        every { dictionaryService.observeDictionary("dict-1") } returns flowOf(dictionary)
        every { wordService.observeWordCounts() } returns flowOf(mapOf("dict-1" to 1), mapOf("dict-1" to 2))
        coEvery { wordService.getWord("word-1") } returns word

        viewModel.init("dict-1", "word-1")
        viewModel.createWordState.first()

        // Re-reading would hand the screen a new instance and re-run its prefill mid-edit.
        coVerify(exactly = 1) { wordService.getWord("word-1") }
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
            sourceInputs = listOf(
                WordFormInput(
                    text = " Haus ",
                    gender = Gender.NEUTER,
                    fields = mapOf(FieldKey.PLURAL to "Häuser"),
                ),
            ),
            targetInputs = listOf(
                WordFormInput(text = "house", fields = mapOf(FieldKey.PLURAL to "houses")),
            ),
        )

        coVerify(exactly = 1) { wordService.saveWord(capture(wordSlot)) }
        val saved = wordSlot.captured
        assertEquals("", saved.wordId)
        assertEquals("dict-1", saved.dictionaryId)
        assertEquals("""["das Haus"]""", saved.word)
        assertEquals("""{"lang":"de","type":"noun","genders":["n"],"fields":{"plural":["Häuser"]}}""", saved.wordMeta)
        assertEquals("""["house"]""", saved.translation)
        assertEquals("""{"lang":"en","type":"noun","fields":{"plural":["houses"]}}""", saved.translationMeta)
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
            sourceInputs = listOf(WordFormInput(text = "Wie geht es dir?")),
            targetInputs = listOf(WordFormInput(text = "How are you?")),
        )

        coVerify(exactly = 1) { wordService.saveWord(capture(wordSlot)) }
        val saved = wordSlot.captured
        assertEquals("""["Wie geht es dir?"]""", saved.word)
        assertEquals("""{"lang":"de"}""", saved.wordMeta)
        assertEquals("""["How are you?"]""", saved.translation)
        assertEquals("""{"lang":"en"}""", saved.translationMeta)
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
            sourceInputs = listOf(WordFormInput(text = "Haus", gender = Gender.NEUTER)),
            targetInputs = listOf(WordFormInput(text = "house")),
        )

        coVerify(exactly = 1) { wordService.saveWord(capture(wordSlot)) }
        val saved = wordSlot.captured
        assertEquals("word-1", saved.wordId)
        assertEquals("""["das Haus"]""", saved.word)
        assertEquals(4, saved.level)
        assertEquals(1_000L, saved.createdAt)
        assertFalse(saved.isSynced)
    }

    @Test
    fun `saveWord does nothing when state is not Success`() = runTest {
        // ViewModel is still in Loading state — init() was never called.
        viewModel.saveWord(
            wordType = WordType.NOUN,
            sourceInputs = listOf(WordFormInput(text = "Haus")),
            targetInputs = listOf(WordFormInput(text = "house")),
        )

        coVerify(exactly = 0) { wordService.saveWord(any()) }
    }

    @Test
    fun `saveWord attempts the save and stays in Success when the save fails`() = runTest {
        // The wordSaved signal and the error snackbar are both replay-0 SharedFlows that cannot be
        // asserted post-hoc; the observable contract is that a failed save is attempted and does
        // not crash or leave the edit form — the state stays Success so the user can retry.
        val dictionary = testDictionaryUi(dictionaryId = "dict-1")
        every { dictionaryService.observeDictionary("dict-1") } returns flowOf(dictionary)
        coEvery { wordService.saveWord(any()) } throws RuntimeException("db error")
        viewModel.init("dict-1")

        viewModel.saveWord(
            wordType = WordType.NOUN,
            sourceInputs = listOf(WordFormInput(text = "Haus")),
            targetInputs = listOf(WordFormInput(text = "house")),
        )

        coVerify(exactly = 1) { wordService.saveWord(any()) }
        assertEquals(CreateWordState.Success(dictionary), viewModel.createWordState.first())
    }

    @Test
    fun `init emits Failed when loading the edited word throws`() = runTest {
        every { dictionaryService.observeDictionary("dict-1") } returns
            flowOf(testDictionaryUi(dictionaryId = "dict-1"))
        coEvery { wordService.getWord("word-1") } throws RuntimeException("db error")

        viewModel.init("dict-1", "word-1")

        assertEquals(CreateWordState.Failed, viewModel.createWordState.first())
    }

    // endregion
}
