package de.coldtea.verborum.bibliotheca.word.ui.createword.model

data class WordFormInput(
    val text: String = "",
    val article: String? = null,
    val plural: String = "",
    val feminine: String = "",
)
