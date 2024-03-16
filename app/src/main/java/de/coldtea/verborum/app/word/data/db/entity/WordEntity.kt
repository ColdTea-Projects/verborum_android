package de.coldtea.verborum.app.word.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.coldtea.verborum.app.common.utils.getNowInMillis

@Entity(tableName = "word", primaryKeys = ["word_id"])
data class WordEntity (
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
    @ColumnInfo(name = "created_at")
    val createdAt: Long = getNowInMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
)