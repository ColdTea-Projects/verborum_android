package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

/**
 * A marketplace dictionary as the Forum screens render it (the details screen imports it from
 * here). Every text field is written by another user — render it as plain text only.
 * [rating] is the average of its star ratings, null while nobody has rated it ([ratingCount] is 0
 * then); [wordCount] is not in the marketplace contract (guide §10) and stays null;
 * [publisherName] is null only when the publisher is unknown.
 */
data class ForumDictionaryUi(
    val dictionaryId: String,
    val publisherId: String,
    val publisherName: String?,
    val name: String,
    val fromLang: String,
    val toLang: String,
    val downloadCount: Int,
    val publishedAt: Long,
    val rating: Float?,
    val wordCount: Int?,
    // Tag codes (resolved to translated labels at render time); empty when there are none.
    val tags: List<String> = emptyList(),
    // How many users rated it — shown beside the average.
    val ratingCount: Int = 0,
    // The user's own listing ("Yours"): never downloadable (the server answers 400).
    val isOwn: Boolean = false,
    // Already in the user's Bibliotheca ("Downloaded").
    val isDownloaded: Boolean = false,
    // A download request is in flight — the action is disabled meanwhile (guide §3 rule 5).
    val isDownloading: Boolean = false,
) {
    /** Whether the Download action is offered at all. */
    val canDownload: Boolean get() = !isOwn && !isDownloaded
}
