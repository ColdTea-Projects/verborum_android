package de.coldtea.verborum.bibliotheca.word.ui.model

/**
 * The UI tier's view of a word. Deliberately one-way: it carries no `isSynced`/`isDeleted`, so it
 * has no `convertToWord()` — converting back would invent those flags and clear a deletion
 * tombstone. Screens that change a word persist the specific field they changed (see
 * `WordService.updateWordLevel`); [de.coldtea.verborum.bibliotheca.word.domain.model.Word] is built
 * from the edit form's own inputs.
 */
data class WordUi(
    val wordId: String,
    val dictionaryId: String,
    val word: String,
    val wordMeta: String,
    val translation: String,
    val translationMeta: String,
    val level: Int,
    val createdAt: Long,
    val updatedAt: Long,
)