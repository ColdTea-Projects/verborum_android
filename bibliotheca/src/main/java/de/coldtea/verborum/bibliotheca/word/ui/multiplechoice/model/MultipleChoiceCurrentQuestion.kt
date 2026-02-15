package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model

data class MultipleChoiceCurrentQuestion(
    val question: MultipleChoiceQuestion,
    val choices: List<String>,
)
