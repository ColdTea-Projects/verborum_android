package de.coldtea.verborum.app.word.ui.model

import de.coldtea.verborum.app.word.data.WordRepository
import de.coldtea.verborum.app.word.data.db.dao.DaoWord
import de.coldtea.verborum.app.word.data.db.entity.WordEntity
import de.coldtea.verborum.app.word.domain.model.Word

data class WordUi(
    val wordId: String,
    val dictionaryId: String,
    val word: String,
    val wordMeta: String,
    val translation: String,
    val translationMeta: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun convertToWord(): Word =
        Word(
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
