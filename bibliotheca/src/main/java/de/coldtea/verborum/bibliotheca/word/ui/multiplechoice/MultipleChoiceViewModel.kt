package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestion
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceQuestion
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MultipleChoiceViewModel @Inject constructor(
    private val wordService: WordService,
) : BaseViewModel() {

    private var currentQuestionIndex = 0
    private var score: Int = 0
    private var questions: List<WordUi> = listOf()

    private var _answered = MutableStateFlow(false)
    val answered = _answered.asSharedFlow()

    private var _selectedAnswer = MutableStateFlow("")
    val selectedAnswer = _selectedAnswer.asSharedFlow()
    private val _currentQuestion =
        MutableStateFlow<MultipleChoiceCurrentQuestionState>(MultipleChoiceCurrentQuestionState.Loading)
    val currentQuestion = _currentQuestion.asSharedFlow()

    fun init(dictionaryId: String) {
        wordService
            .observeWordsByDictionary(dictionaryId)
            .observe(
                onSuccess = { words ->
                    if (words.distinctBy { it.word + it.translation }.size < 4) _currentQuestion.emit(
                        MultipleChoiceCurrentQuestionState.NotEnoughWords
                    )

                    if (questions.isEmpty()) {
                        questions = words.shuffled()
                        initNextQuestion()
                    }
                },
                onError = {
                    _currentQuestion.emit(MultipleChoiceCurrentQuestionState.Failed)
                }
            )
    }

    fun onAnswerReceived(answer: String) = viewModelScope.launch {
        _selectedAnswer.emit(answer)
    }

    fun onAnswerGiven() = viewModelScope.launch {
        val question = _currentQuestion.value
        if (question is MultipleChoiceCurrentQuestionState.Success) {
            val currentQuestionWordUiId = question.multipleChoiceCurrentQuestion.question.wordId
            val correctAnswer = question.multipleChoiceCurrentQuestion.question.answer
            if (correctAnswer == _selectedAnswer.value) {
                score += 1
                updateLevel(currentQuestionWordUiId, true)
                _snackbarMessages.emit("Correct answer!")
            } else {
                updateLevel(currentQuestionWordUiId, false)
                _snackbarMessages.emit("Incorrect, correct answer was $correctAnswer")
            }
            _answered.emit(true)
        }
    }

    fun onNextQuestionRequested() = viewModelScope.launch {
        _selectedAnswer.emit("")
        _answered.emit(false)
        currentQuestionIndex += 1
        initNextQuestion()
    }

    fun onRetryClicked() = viewModelScope.launch {
        _selectedAnswer.emit("")
        _answered.emit(false)
        score = 0
        currentQuestionIndex = 0
        initNextQuestion()
    }

    private suspend fun initNextQuestion() {
        val nextQuestionState = if (currentQuestionIndex == questions.size) {
            MultipleChoiceCurrentQuestionState.Completed(
                passed = score > (questions.size / 2),
                percentage = ((score.toDouble() / questions.size.toDouble()) * 100).toInt(),
                correctAnswers = score,
                totalQuestions = questions.size
            )
        } else {
            val word = questions[currentQuestionIndex]
            val question = MultipleChoiceQuestion(word.wordId, word.word, word.translation)
            val currentQuestion = MultipleChoiceCurrentQuestion(
                question = question,
                choices = questions.prepareChoices(question.answer)
            )

            MultipleChoiceCurrentQuestionState.Success(
                multipleChoiceCurrentQuestion = currentQuestion,
                index = currentQuestionIndex + 1,
                size = questions.size
            )
        }

        _currentQuestion.emit(nextQuestionState)
    }

    private fun List<WordUi>.prepareChoices(
        exclude: String,
        count: Int = 3
    ): List<String> {
        return this
            .map { it.translation }
            .distinct()
            .filter { it != exclude }
            .shuffled()
            .take(count)
            .plus(exclude)
            .shuffled()
    }

    private fun updateLevel(wordId: String, isCorrect: Boolean) = viewModelScope.launch {
        val wordUi = questions.first { it.wordId == wordId }
        val newLevel = if (isCorrect) minOf(7, wordUi.level + 1) else maxOf(0, wordUi.level - 1)

        wordService.saveWord(wordUi.convertToWord().copy(level = newLevel))
    }
}