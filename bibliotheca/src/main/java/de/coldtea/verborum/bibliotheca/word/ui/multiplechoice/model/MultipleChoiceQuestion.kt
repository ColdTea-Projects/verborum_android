package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model

import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey

/**
 * One quiz item. A word entry produces a base question (word → translation) plus one question per
 * grammatical form present on both sides ([formKey] names the form, null for the base question).
 */
data class MultipleChoiceQuestion(
    val wordId: String,
    val question: String,
    val answer: String,
    val formKey: FieldKey? = null,
)
