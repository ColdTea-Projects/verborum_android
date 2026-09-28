package de.coldtea.verborum.forum.marketplace.domain.model

import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumWordUi

/** A read-only word of someone else's public dictionary; [surfaces] are its alternatives. */
data class MarketplaceWord(
    val wordId: String,
    val dictionaryId: String,
    val surfaces: List<String>,
    val translations: List<String>,
) {
    fun convertToUi() = ForumWordUi(
        wordId = wordId,
        word = surfaces.joinToString(SURFACE_SEPARATOR),
        translation = translations.joinToString(SURFACE_SEPARATOR),
    )

    private companion object {
        const val SURFACE_SEPARATOR = "/"
    }
}
