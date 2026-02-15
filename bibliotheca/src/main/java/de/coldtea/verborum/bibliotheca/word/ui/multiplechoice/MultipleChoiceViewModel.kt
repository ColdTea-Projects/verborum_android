package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestion
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceQuestion
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MultipleChoiceViewModel @Inject constructor(
    private val wordService: WordService,
) : BaseViewModel() {

    private var currentQuestionIndex = 0
    private var score: Int = 0
    private var questions: List<MultipleChoiceQuestion> = listOf()

    private var _feedback = MutableStateFlow("")
    val feedback = _feedback.asSharedFlow()
    private val _currentQuestion =
        MutableStateFlow<MultipleChoiceCurrentQuestionState>(MultipleChoiceCurrentQuestionState.Loading)
    val currentQuestion = _currentQuestion.asSharedFlow()

    fun init(dictionaryId: String) {
        wordService
            .observeWordsByDictionary(dictionaryId)
            .observe(
                onSuccess = { words ->
                    if (words.size < 4) _currentQuestion.emit(MultipleChoiceCurrentQuestionState.NotEnoughWords)

                    if(questions.isEmpty()){
                        questions = words.map { word ->
                            MultipleChoiceQuestion(word.wordId, word.word, word.translation)
                        }.shuffled()
                        initNextQuestion()
                    }
                },
                onError = {
                    _currentQuestion.emit(MultipleChoiceCurrentQuestionState.Failed)
                }
            )
    }

    fun onAnswerReceived(answer: String) = viewModelScope.launch {
        val question = _currentQuestion.value
        if (question is MultipleChoiceCurrentQuestionState.Success) {
            val correctAnswer = question.multipleChoiceCurrentQuestion.question.answer
            if (correctAnswer == answer) {
                score += 1
                _feedback.emit("Correct")
            } else {
                _feedback.emit("Incorrect, correct answer was $correctAnswer")
            }
        }
    }

    fun onNextQuestionRequested() = viewModelScope.launch {
        _feedback.emit("")
        currentQuestionIndex += 1
        initNextQuestion()
    }

    private suspend fun initNextQuestion() {
        val question = questions[currentQuestionIndex]
        val currentQuestion = MultipleChoiceCurrentQuestion(
            question = question,
            choices = questions.prepareChoices(question.answer)
        )
        _currentQuestion.emit(
            MultipleChoiceCurrentQuestionState.Success(
                multipleChoiceCurrentQuestion = currentQuestion,
                index = currentQuestionIndex + 1,
                size = questions.size
            )
        )
    }

    private fun List<MultipleChoiceQuestion>.prepareChoices(
        exclude: String,
        count: Int = 3
    ): List<String> {
        return this
            .map { it.answer }
            .filter { it != exclude }
            .shuffled()
            .take(count)
            .plus(exclude)
    }
}