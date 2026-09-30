package de.coldtea.verborum.forum.marketplace.domain.model

import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi

/** A public dictionary offered in the marketplace. Null fields are not provided by the backend yet. */
data class MarketplaceListing(
    val dictionaryId: String,
    val publisherId: String,
    val publisherName: String?,
    val name: String,
    val fromLang: String,
    val toLang: String,
    val importCount: Int,
    val publishedAt: Long,
    val rating: Float?,
    val wordCount: Int?,
    val tags: List<String>,
) {
    fun convertToUi() = ForumDictionaryUi(
        dictionaryId = dictionaryId,
        publisherId = publisherId,
        publisherName = publisherName,
        name = name,
        fromLang = fromLang,
        toLang = toLang,
        downloadCount = importCount,
        publishedAt = publishedAt,
        rating = rating,
        wordCount = wordCount,
        tags = tags,
    )
}
