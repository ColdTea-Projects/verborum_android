package de.coldtea.verborum.bibliotheca.word.ui.createword.model

/**
 * User input for one language side of a word entry. [text] is the base word (always required),
 * [gender] backs the article/gender selector, and [fields] holds every other grammatical form
 * keyed by [FieldKey].
 */
data class WordFormInput(
    val text: String = "",
    val gender: Gender? = null,
    val fields: Map<FieldKey, String> = emptyMap(),
) {
    fun field(key: FieldKey): String = fields[key].orEmpty()

    fun withField(key: FieldKey, value: String): WordFormInput =
        copy(fields = fields + (key to value))
}
