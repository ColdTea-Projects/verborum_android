package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice

import de.coldtea.verborum.bibliotheca.testWordUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceQuestion
import de.coldtea.verborum.core.BaseTest
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
import org.junit.Assert.assertTrue
import org.junit.Test

class MultipleChoiceViewModelTest : BaseTest() {

    @MockK
    private lateinit var wordService: WordService

    private lateinit var viewModel: MultipleChoiceViewModel

    override fun setUp() {
        super.setUp()
        viewModel = MultipleChoiceViewModel(wordService)
    }

    /** Builds [count] words with pairwise distinct word/translation pairs. */
    private fun distinctWords(count: Int, level: Int = 0): List<WordUi> = (1..count).map {
        testWordUi(
            wordId = "w-$it",
            word = "word-$it",
            translation = "translation-$it",
            level = level,
        )
    }

    private fun initWith(words: List<WordUi>, dictionaryId: String = "dict-1") {
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(words)
        viewModel.init(dictionaryId)
    }

    /** Questions are shuffled, so the current question is always read back from the state. */
    private suspend fun currentSuccess(): MultipleChoiceCurrentQuestionState.Success =
        viewModel.currentQuestion.first() as MultipleChoiceCurrentQuestionState.Success

    private suspend fun answerCurrentQuestion(correctly: Boolean) {
        val question = currentSuccess().multipleChoiceCurrentQuestion.question
        val answer = if (correctly) question.answer else "definitely-wrong-answer"
        viewModel.onAnswerReceived(answer)
        viewModel.onAnswerGiven()
    }

    // region initial state

    @Test
    fun `initial currentQuestion is Loading`() = runTest {
        assertEquals(
            MultipleChoiceCurrentQuestionState.Loading,
            viewModel.currentQuestion.first(),
        )
    }

    @Test
    fun `initially nothing is answered and no answer is selected`() = runTest {
        assertFalse(viewModel.answered.first())
        assertEquals("", viewModel.selectedAnswer.first())
    }

    // endregion

    // region init

    @Test
    fun `init emits Success with the first question and four choices containing the answer`() = runTest {
        val words = distinctWords(5)

        initWith(words)

        val state = currentSuccess()
        assertEquals(1, state.index)
        assertEquals(5, state.size)

        val question = state.multipleChoiceCurrentQuestion.question
        val source = words.first { it.wordId == question.wordId }
        assertEquals(source.word, question.question)
        assertEquals(source.translation, question.answer)

        val choices = state.multipleChoiceCurrentQuestion.choices
        assertEquals(4, choices.size)
        assertEquals(choices.size, choices.distinct().size)
        assertTrue(choices.contains(question.answer))
        assertTrue(words.map { it.translation }.containsAll(choices))
    }

    @Test
    fun `init emits Failed when words flow throws`() = runTest {
        every { wordService.observeWordsByDictionary("dict-1") } returns flow {
            throw RuntimeException("db error")
        }

        viewModel.init("dict-1")

        assertEquals(
            MultipleChoiceCurrentQuestionState.Failed,
            viewModel.currentQuestion.first(),
        )
    }

    @Test
    fun `init with fewer than four distinct words emits NotEnoughWords`() = runTest {
        // Regression guard: NotEnoughWords used to be overwritten by a Success question
        // with fewer than four choices because init() fell through after emitting it.
        initWith(distinctWords(2))

        assertEquals(
            MultipleChoiceCurrentQuestionState.NotEnoughWords,
            viewModel.currentQuestion.first(),
        )
    }

    @Test
    fun `init with an empty word list emits NotEnoughWords`() = runTest {
        // Regression guard: an empty dictionary used to end in a failed 0-of-0 Completed state.
        initWith(emptyList())

        assertEquals(
            MultipleChoiceCurrentQuestionState.NotEnoughWords,
            viewModel.currentQuestion.first(),
        )
    }

    // endregion

    // region grammatical form questions

    /** Builds [count] verbs with past and participle forms on both language sides. */
    private fun verbWords(count: Int): List<WordUi> = (1..count).map {
        testWordUi(
            wordId = "w-$it",
            word = "verb-$it",
            translation = "verbo-$it",
            wordMeta = """{"lang":"en","type":"verb","fields":{"past":["past-$it"],"participle":["part-$it"]}}""",
            translationMeta = """{"lang":"de","type":"verb","fields":{"past":["pastde-$it"],"participle":["partde-$it"],"aux":["sein"]}}""",
        )
    }

    /** Walks through the whole quiz collecting every question in presentation order. */
    private suspend fun collectAllQuestions(): List<MultipleChoiceQuestion> {
        val size = currentSuccess().size
        return (1..size).map {
            val question = currentSuccess().multipleChoiceCurrentQuestion.question
            viewModel.onNextQuestionRequested()
            question
        }
    }

    @Test
    fun `a word with two shared forms generates three questions`() = runTest {
        initWith(verbWords(4))

        assertEquals(12, currentSuccess().size)
    }

    @Test
    fun `form questions pair the source form with the target form of the same word`() = runTest {
        initWith(verbWords(4))

        val pastQuestions = collectAllQuestions().filter { it.formKey == FieldKey.PAST }

        assertEquals(4, pastQuestions.size)
        pastQuestions.forEach { question ->
            val id = question.wordId.removePrefix("w-")
            assertEquals("past-$id", question.question)
            assertEquals("pastde-$id", question.answer)
        }
    }

    @Test
    fun `participle questions carry the auxiliary in both directions`() = runTest {
        initWith(verbWords(4))

        val participleQuestions = collectAllQuestions().filter { it.formKey == FieldKey.PARTICIPLE }

        assertEquals(4, participleQuestions.size)
        participleQuestions.forEach { question ->
            val id = question.wordId.removePrefix("w-")
            assertEquals("part-$id", question.question)
            assertEquals("(sein) partde-$id", question.answer)
        }
    }

    @Test
    fun `no auxiliary-only question is generated`() = runTest {
        initWith(verbWords(4))

        assertTrue(collectAllQuestions().none { it.formKey == FieldKey.AUXILIARY })
    }

    @Test
    fun `no form question is generated when only one side has the form`() = runTest {
        val words = (1..4).map {
            testWordUi(
                wordId = "w-$it",
                word = "verb-$it",
                translation = "verbo-$it",
                wordMeta = """{"lang":"en","type":"verb","fields":{"past":["past-$it"]}}""",
                translationMeta = """{"lang":"de","type":"verb"}""",
            )
        }

        initWith(words)

        assertEquals(4, currentSuccess().size)
    }

    @Test
    fun `form question choices are drawn from answers of the same form`() = runTest {
        initWith(verbWords(4))
        val pastAnswers = (1..4).map { "pastde-$it" }

        repeat(currentSuccess().size) {
            val current = currentSuccess().multipleChoiceCurrentQuestion
            if (current.question.formKey == FieldKey.PAST) {
                assertEquals(4, current.choices.size)
                assertTrue(pastAnswers.containsAll(current.choices))
            }
            viewModel.onNextQuestionRequested()
        }
    }

    // endregion

    // region answering

    @Test
    fun `onAnswerReceived emits the selected answer`() = runTest {
        viewModel.onAnswerReceived("translation-3")

        assertEquals("translation-3", viewModel.selectedAnswer.first())
    }

    @Test
    fun `correct answer saves the word with incremented level and marks answered`() = runTest {
        val words = distinctWords(4, level = 2)
        initWith(words)
        val source = words.first { it.wordId == currentSuccess().multipleChoiceCurrentQuestion.question.wordId }

        answerCurrentQuestion(correctly = true)

        val savedWord = slot<Word>()
        coVerify(exactly = 1) { wordService.saveWord(capture(savedWord)) }
        assertEquals(source.convertToWord().copy(level = 3), savedWord.captured)
        assertTrue(viewModel.answered.first())
    }

    @Test
    fun `incorrect answer saves the word with decremented level and marks answered`() = runTest {
        val words = distinctWords(4, level = 3)
        initWith(words)
        val source = words.first { it.wordId == currentSuccess().multipleChoiceCurrentQuestion.question.wordId }

        answerCurrentQuestion(correctly = false)

        val savedWord = slot<Word>()
        coVerify(exactly = 1) { wordService.saveWord(capture(savedWord)) }
        assertEquals(source.convertToWord().copy(level = 2), savedWord.captured)
        assertTrue(viewModel.answered.first())
    }

    @Test
    fun `correct answer does not raise the level above seven`() = runTest {
        initWith(distinctWords(4, level = 7))

        answerCurrentQuestion(correctly = true)

        val savedWord = slot<Word>()
        coVerify(exactly = 1) { wordService.saveWord(capture(savedWord)) }
        assertEquals(7, savedWord.captured.level)
    }

    @Test
    fun `incorrect answer does not lower the level below zero`() = runTest {
        initWith(distinctWords(4, level = 0))

        answerCurrentQuestion(correctly = false)

        val savedWord = slot<Word>()
        coVerify(exactly = 1) { wordService.saveWord(capture(savedWord)) }
        assertEquals(0, savedWord.captured.level)
    }

    @Test
    fun `onAnswerGiven does nothing when there is no current question`() = runTest {
        // ViewModel is still in Loading state — init() was never called.
        viewModel.onAnswerReceived("translation-1")
        viewModel.onAnswerGiven()

        coVerify(exactly = 0) { wordService.saveWord(any()) }
        assertFalse(viewModel.answered.first())
    }

    // endregion

    // region per-word scoring and leveling

    /** Walks the whole quiz, deciding per question: true = answer correctly, false = wrong, null = skip. */
    private suspend fun walkAnswering(decide: (MultipleChoiceQuestion) -> Boolean?) {
        val size = currentSuccess().size
        repeat(size) {
            val question = currentSuccess().multipleChoiceCurrentQuestion.question
            when (decide(question)) {
                true -> answerCurrentQuestion(correctly = true)
                false -> answerCurrentQuestion(correctly = false)
                null -> Unit
            }
            viewModel.onNextQuestionRequested()
        }
    }

    @Test
    fun `a word answered correctly in several forms raises its level only once`() = runTest {
        initWith(verbWords(4).map { it.copy(level = 2) })

        // w-1 produces three questions (base, past, participle); answer all of them correctly.
        walkAnswering { if (it.wordId == "w-1") true else null }

        val saved = mutableListOf<Word>()
        coVerify(exactly = 1) { wordService.saveWord(capture(saved)) }
        assertEquals("w-1", saved.single().wordId)
        assertEquals(3, saved.single().level)
    }

    @Test
    fun `a word answered wrong in several forms lowers its level only once`() = runTest {
        initWith(verbWords(4).map { it.copy(level = 2) })

        walkAnswering { if (it.wordId == "w-1") false else null }

        val saved = mutableListOf<Word>()
        coVerify(exactly = 1) { wordService.saveWord(capture(saved)) }
        assertEquals(1, saved.single().level)
    }

    @Test
    fun `a word answered both correctly and wrong nets no level change`() = runTest {
        initWith(verbWords(4).map { it.copy(level = 4) })

        walkAnswering {
            when {
                it.wordId != "w-1" -> null
                it.formKey == null -> true            // base form correct
                it.formKey == FieldKey.PAST -> false  // past form wrong
                else -> null
            }
        }

        // One raise and one lower for the same word settle back to its stored level, whatever order.
        val saved = mutableListOf<Word>()
        coVerify { wordService.saveWord(capture(saved)) }
        assertEquals("w-1", saved.last().wordId)
        assertEquals(4, saved.last().level)
    }

    @Test
    fun `score counts every correct form question, not just the word`() = runTest {
        initWith(verbWords(4)) // 4 words, 12 questions (base + past + participle each)

        // Every form of two words correct (6 questions), every form of the other two wrong (6).
        walkAnswering { it.wordId == "w-1" || it.wordId == "w-2" }

        assertEquals(
            MultipleChoiceCurrentQuestionState.Completed(
                passed = false,
                percentage = 50,
                correctAnswers = 6,
                totalQuestions = 12,
            ),
            viewModel.currentQuestion.first(),
        )
    }

    @Test
    fun `each correct form question adds to the score independently`() = runTest {
        initWith(verbWords(4)) // 12 questions

        // Only w-1's base form is answered correctly; every other question is wrong.
        walkAnswering { it.wordId == "w-1" && it.formKey == null }

        val completed = viewModel.currentQuestion.first() as MultipleChoiceCurrentQuestionState.Completed
        assertEquals(1, completed.correctAnswers)
        assertEquals(12, completed.totalQuestions)
        assertEquals(8, completed.percentage) // 1 / 12 → 8%
    }

    // endregion

    // region navigation and completion

    @Test
    fun `onNextQuestionRequested advances to the next question and resets the answer state`() = runTest {
        initWith(distinctWords(4))
        answerCurrentQuestion(correctly = true)

        viewModel.onNextQuestionRequested()

        val state = currentSuccess()
        assertEquals(2, state.index)
        assertEquals(4, state.size)
        assertFalse(viewModel.answered.first())
        assertEquals("", viewModel.selectedAnswer.first())
    }

    @Test
    fun `answering every question correctly completes as passed with full percentage`() = runTest {
        initWith(distinctWords(4))

        repeat(4) {
            answerCurrentQuestion(correctly = true)
            viewModel.onNextQuestionRequested()
        }

        assertEquals(
            MultipleChoiceCurrentQuestionState.Completed(
                passed = true,
                percentage = 100,
                correctAnswers = 4,
                totalQuestions = 4,
            ),
            viewModel.currentQuestion.first(),
        )
    }

    @Test
    fun `answering exactly half correctly completes as not passed`() = runTest {
        initWith(distinctWords(4))

        repeat(2) {
            answerCurrentQuestion(correctly = true)
            viewModel.onNextQuestionRequested()
        }
        repeat(2) {
            answerCurrentQuestion(correctly = false)
            viewModel.onNextQuestionRequested()
        }

        assertEquals(
            MultipleChoiceCurrentQuestionState.Completed(
                passed = false,
                percentage = 50,
                correctAnswers = 2,
                totalQuestions = 4,
            ),
            viewModel.currentQuestion.first(),
        )
    }

    @Test
    fun `onNextQuestionRequested after completion keeps the Completed state`() = runTest {
        // Regression guard: a further next-question request past Completed used to throw
        // IndexOutOfBoundsException inside viewModelScope and crash the app.
        initWith(distinctWords(4))
        repeat(4) {
            answerCurrentQuestion(correctly = true)
            viewModel.onNextQuestionRequested()
        }

        viewModel.onNextQuestionRequested()

        assertEquals(
            MultipleChoiceCurrentQuestionState.Completed(
                passed = true,
                percentage = 100,
                correctAnswers = 4,
                totalQuestions = 4,
            ),
            viewModel.currentQuestion.first(),
        )
    }

    @Test
    fun `onRetryClicked restarts from the first question with a reset score`() = runTest {
        initWith(distinctWords(4))
        answerCurrentQuestion(correctly = true)
        repeat(4) { viewModel.onNextQuestionRequested() }

        viewModel.onRetryClicked()

        val state = currentSuccess()
        assertEquals(1, state.index)
        assertFalse(viewModel.answered.first())
        assertEquals("", viewModel.selectedAnswer.first())

        // Skipping through without answering proves the previous score was reset.
        repeat(4) { viewModel.onNextQuestionRequested() }
        assertEquals(
            MultipleChoiceCurrentQuestionState.Completed(
                passed = false,
                percentage = 0,
                correctAnswers = 0,
                totalQuestions = 4,
            ),
            viewModel.currentQuestion.first(),
        )
    }

    // endregion
}
