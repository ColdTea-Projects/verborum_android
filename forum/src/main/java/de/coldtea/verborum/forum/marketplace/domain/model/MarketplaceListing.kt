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
        // Ratings and word counts are not in the marketplace contract (guide §10).
        rating = null,
        wordCount = null,
        tags = tags,
    )
}
