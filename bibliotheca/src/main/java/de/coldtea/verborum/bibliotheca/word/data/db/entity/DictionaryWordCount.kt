package de.coldtea.verborum.bibliotheca.word.data.db.entity

import androidx.room.ColumnInfo

/** Room query projection: number of live (non-tombstoned) words per dictionary. */
data class DictionaryWordCount(
    @ColumnInfo(name = "fk_dictionary_id")
    val dictionaryId: String,
    @ColumnInfo(name = "word_count")
    val count: Int,
)
