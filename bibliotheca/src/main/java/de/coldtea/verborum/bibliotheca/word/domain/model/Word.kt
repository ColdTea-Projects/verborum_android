package de.coldtea.verborum.bibliotheca.word.domain.model

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.data.db.dao.DaoWord
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi

data class Word(
    val wordId: String,
    val dictionaryId: String,
    val word: String,
    val wordMeta: String,
    val translation: String,
    val translationMeta: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun convertToEntity(): WordEntity =
        WordEntity(
            wordId = wordId,
            dictionaryId = dictionaryId,
            word = word,
            wordMeta = wordMeta,
            translation = translation,
            translationMeta = translationMeta,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun convertToUi(): WordUi =
        WordUi(
            wordId = wordId,
            dictionaryId = dictionaryId,
            word = word,
            wordMeta = wordMeta,
            translation = translation,
            translationMeta = translationMeta,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
