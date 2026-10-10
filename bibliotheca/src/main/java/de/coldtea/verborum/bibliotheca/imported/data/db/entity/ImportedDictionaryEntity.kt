package de.coldtea.verborum.bibliotheca.imported.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionary

/**
 * A dictionary downloaded (imported) from the Forum: a read-only local copy of someone else's
 * public dictionary. Its own table on purpose — the sync engine only ever reads `dictionary`, so
 * nothing here can be uploaded (every write to it would be a 403, guide §6.9).
 */
@Entity(tableName = "imported_dictionary", primaryKeys = ["dictionary_id"])
data class ImportedDictionaryEntity(
    @ColumnInfo(name = "dictionary_id")
    val dictionaryId: String,
    // The owner's `sub`.
    @ColumnInfo(name = "publisher_id")
    val publisherId: String,
    // Only known from the Forum listing it was downloaded from; null when downloaded elsewhere.
    @ColumnInfo(name = "publisher_name")
    val publisherName: String?,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "from_lang")
    val fromLang: String,
    @ColumnInfo(name = "to_lang")
    val toLang: String,
    // JSON array of tag codes, like `dictionary.tags`.
    @ColumnInfo(name = "tags")
    val tags: String,
    @ColumnInfo(name = "imported_at")
    val importedAt: Long,
) {
    fun convertToImportedDictionary() = ImportedDictionary(
        dictionaryId = dictionaryId,
        publisherId = publisherId,
        publisherName = publisherName,
        name = name,
        fromLang = fromLang,
        toLang = toLang,
        tags = DictionaryEntity.decodeTags(tags),
        importedAt = importedAt,
    )
}
