package de.coldtea.verborum.forum.marketplace.domain.model

/** Two wire language codes (uppercase) searched together, in either direction. */
data class MarketplaceLanguagePair(
    val first: String,
    val second: String,
) {
    /** The `pair` query value: "EN-TR" (guide §5.1). */
    val wireValue: String get() = "$first-$second"
}
