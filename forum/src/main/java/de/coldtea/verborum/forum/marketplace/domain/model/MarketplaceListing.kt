package de.coldtea.verborum.forum.marketplace.domain.model

import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi

/** A public dictionary offered in the marketplace. */
data class MarketplaceListing(
    val dictionaryId: String,
    val publisherId: String,
    // Null only when the publisher's name is unknown (a details screen opened without the list).
    val publisherName: String?,
    val name: String,
    val fromLang: String,
    val toLang: String,
    val importCount: Int,
    // Null while nobody has rated it.
    val ratingAverage: Float?,
    val ratingCount: Int,
    val publishedAt: Long,
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
        rating = ratingAverage,
        ratingCount = ratingCount,
        // Listings carry no word count (guide §10).
        wordCount = null,
        tags = tags,
    )
}
