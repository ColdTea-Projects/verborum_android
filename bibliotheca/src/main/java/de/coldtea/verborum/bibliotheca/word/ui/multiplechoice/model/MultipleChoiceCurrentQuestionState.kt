package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model

sealed class MultipleChoiceCurrentQuestionState {
    data object Failed : MultipleChoiceCurrentQuestionState()
    data object NotEnoughWords : MultipleChoiceCurrentQuestionState()
    data object Loading : MultipleChoiceCurrentQuestionState()
    data class Success(
        val multipleChoiceCurrentQuestion: MultipleChoiceCurrentQuestion,
        val index: Int,
        val size: Int,
    ) : MultipleChoiceCurrentQuestionState()

    data class Completed(val score: Int) : MultipleChoiceCurrentQuestionState()
}
