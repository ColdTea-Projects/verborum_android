package de.coldtea.verborum.forum

import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceWord
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumWordUi
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi

fun testMarketplaceListing(
    dictionaryId: String = "dictionaryId",
    publisherId: String = "publisherId",
    publisherName: String? = "Anna Schmidt",
    name: String = "Everyday German",
    fromLang: String = "EN",
    toLang: String = "DE",
    importCount: Int = 12,
    publishedAt: Long = 1_000L,
    rating: Float? = 4.5f,
    wordCount: Int? = 6,
    tags: List<String> = listOf("a1", "food_drink"),
) = MarketplaceListing(
    dictionaryId = dictionaryId,
    publisherId = publisherId,
    publisherName = publisherName,
    name = name,
    fromLang = fromLang,
    toLang = toLang,
    importCount = importCount,
    publishedAt = publishedAt,
    rating = rating,
    wordCount = wordCount,
    tags = tags,
)

fun testForumDictionaryUi(
    dictionaryId: String = "dictionaryId",
    publisherId: String = "publisherId",
    publisherName: String? = "Anna Schmidt",
    name: String = "Everyday German",
    fromLang: String = "EN",
    toLang: String = "DE",
    downloadCount: Int = 12,
    publishedAt: Long = 1_000L,
    rating: Float? = 4.5f,
    wordCount: Int? = 6,
    tags: List<String> = listOf("a1", "food_drink"),
) = ForumDictionaryUi(
    dictionaryId = dictionaryId,
    publisherId = publisherId,
    publisherName = publisherName,
    name = name,
    fromLang = fromLang,
    toLang = toLang,
    downloadCount = downloadCount,
    publishedAt = publishedAt,
    rating = rating,
    wordCount = wordCount,
    tags = tags,
)

fun testMarketplaceWord(
    wordId: String = "wordId",
    dictionaryId: String = "dictionaryId",
    surfaces: List<String> = listOf("buy", "purchase"),
    translations: List<String> = listOf("kaufen"),
) = MarketplaceWord(
    wordId = wordId,
    dictionaryId = dictionaryId,
    surfaces = surfaces,
    translations = translations,
)

fun testForumWordUi(
    wordId: String = "wordId",
    word: String = "buy/purchase",
    translation: String = "kaufen",
) = ForumWordUi(
    wordId = wordId,
    word = word,
    translation = translation,
)
