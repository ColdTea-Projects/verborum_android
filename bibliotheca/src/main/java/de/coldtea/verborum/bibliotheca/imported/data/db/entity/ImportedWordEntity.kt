package de.coldtea.verborum.bibliotheca.imported.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedWord

/**
 * A word of a downloaded dictionary. Read-only, except [level]: the user's own practice progress,
 * kept on this device only (the server's level on someone else's word is always null, guide §6.8).
 */
@Entity(tableName = "imported_word", primaryKeys = ["word_id"])
data class ImportedWordEntity(
    @ColumnInfo(name = "word_id")
    val wordId: String,
    @ColumnInfo(name = "fk_dictionary_id")
    val dictionaryId: String,
    @ColumnInfo(name = "word")
    val word: String,
    @ColumnInfo(name = "word_meta")
    val wordMeta: String,
    @ColumnInfo(name = "translation")
    val translation: String,
    @ColumnInfo(name = "translation_meta")
    val translationMeta: String,
    @ColumnInfo(name = "level")
    val level: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
) {
    fun convertToImportedWord() = ImportedWord(
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
