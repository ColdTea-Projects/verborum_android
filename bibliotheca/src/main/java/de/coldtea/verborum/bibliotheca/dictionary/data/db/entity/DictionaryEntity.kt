package de.coldtea.verborum.bibliotheca.dictionary.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "from_lang")
    val fromLang: String,
    @ColumnInfo(name = "to_lang")
    val toLang: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = getNowInMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
    // Tag codes stored as a JSON array of stable codes, e.g. ["food_drink","a1"]. The backend keeps
    // tags in a separate sub-resource (see docs/dictionary-tags-api.md); locally we keep this flat
    // list and reconcile it against that endpoint during sync.
    @ColumnInfo(name = "tags")
    val tags: String = EMPTY_TAGS,
){
    fun convertToDictionary() = Dictionary(
        dictionaryId = dictionaryId,
        userId = userId,
        name = name,
        isPublic = isPublic,
        isSynced = isSynced,
        isDeleted = isDeleted,
        fromLang = fromLang,
        toLang = toLang,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = decodeTags(tags),
    )

    companion object {
        const val GUEST_USER_ID = "00000000-0000-0000-0000-000000000000"
        const val EMPTY_TAGS = "[]"

        private val tagsJson = Json { ignoreUnknownKeys = true }

        fun encodeTags(tags: List<String>): String = tagsJson.encodeToString(tags)

        fun decodeTags(raw: String): List<String> =
            runCatching { tagsJson.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
    }
}

