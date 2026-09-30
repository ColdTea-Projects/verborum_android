package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

/**
 * A marketplace dictionary as the Forum screens render it (the details screen imports it from
 * here). Every text field is written by another user — render it as plain text only.
 * [publisherName], [rating] and [wordCount] are null when the backend does not provide them.
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
)
