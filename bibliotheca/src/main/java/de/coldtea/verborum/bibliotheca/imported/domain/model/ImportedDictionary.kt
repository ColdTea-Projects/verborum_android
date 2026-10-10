package de.coldtea.verborum.bibliotheca.imported.domain.model

import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedDictionaryEntity

/** A downloaded dictionary — someone else's, read-only. */
data class ImportedDictionary(
    val dictionaryId: String,
    val publisherId: String,
    val publisherName: String?,
    val name: String,
    val fromLang: String,
    val toLang: String,
    val tags: List<String>,
    val importedAt: Long,
) {
    fun convertToEntity() = ImportedDictionaryEntity(
        dictionaryId = dictionaryId,
        publisherId = publisherId,
        publisherName = publisherName,
        name = name,
        fromLang = fromLang,
        toLang = toLang,
        tags = DictionaryEntity.encodeTags(tags),
        importedAt = importedAt,
    )

    fun convertToUi() = DictionaryUi(
        dictionaryId = dictionaryId,
        userId = publisherId,
        name = name,
        isPublic = true,
        fromLang = fromLang,
        toLang = toLang,
        // In the list a download sorts by when it was downloaded.
        createdAt = importedAt,
        updatedAt = importedAt,
        tags = tags,
        isImported = true,
        publisherName = publisherName,
    )
}
