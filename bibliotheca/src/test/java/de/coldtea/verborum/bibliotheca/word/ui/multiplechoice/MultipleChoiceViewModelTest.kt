package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice

import de.coldtea.verborum.bibliotheca.testWordUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
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
