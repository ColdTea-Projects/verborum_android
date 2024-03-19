package de.coldtea.verborum.bibliotheca.dictionary.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis

@Entity(tableName = "dictionary", primaryKeys = ["dictionary_id"])
data class DictionaryEntity (
    @ColumnInfo(name = "dictionary_id")
    val dictionaryId: String,
    @ColumnInfo(name = "fk_user_id")
    val userId: String = GUEST_USER_ID,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "is_public")
    val isPublic: Boolean = false,
    @ColumnInfo(name = "from_lang")
    val fromLang: String,
    @ColumnInfo(name = "to_lang")
    val toLang: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = getNowInMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
){
    fun convertToDictionary() = Dictionary(
        dictionaryId = dictionaryId,
        userId = userId,
        name = name,
        isPublic = isPublic,
        fromLang = fromLang,
        toLang = toLang,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        const val GUEST_USER_ID = "00000000-0000-0000-0000-000000000000"
    }
}

