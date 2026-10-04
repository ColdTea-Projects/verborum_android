package de.coldtea.verborum.core.library.domain.model

/** A dictionary's language direction and when it was last touched (epoch millis). */
data class DictionaryLanguages(
    val fromLang: String,
    val toLang: String,
    val lastActivityAt: Long,
)
