package de.coldtea.verborum.bibliotheca.imported.domain.model

import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedWordEntity
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi

/** A word of a downloaded dictionary; [level] is the user's own progress, device-local. */
data class ImportedWord(
    val wordId: String,
    val dictionaryId: String,
    val word: String,
    val wordMeta: String,
    val translation: String,
    val translationMeta: String,
    val level: Int,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun convertToEntity() = ImportedWordEntity(
        wordId = wordId,
        dictionaryId = dictionaryId,
        word = word,
        wordMeta = wordMeta,
        translation = translation,
        translationMeta = translationMeta,
        level = level,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun convertToUi() = WordUi(
        wordId = wordId,
        dictionaryId = dictionaryId,
        word = word,
        wordMeta = wordMeta,
        translation = translation,
        translationMeta = translationMeta,
        level = level,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
