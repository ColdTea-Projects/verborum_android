package de.coldtea.verborum.core.options.ui.model

/**
 * A language the app's own UI is translated into — one entry per `values-XX/` locale, and the list
 * the language picker offers below "System default".
 *
 * [endonym] is each language's name *in that language*, deliberately not a translated resource: a
 * user hunting for their language recognizes "Türkçe" or "日本語" whatever the current UI language
 * is, and a name in its own language needs no 19×19 translation matrix.
 *
 * This mirrors `SupportedLanguage` in bibliotheca (the dictionary languages), which `core` cannot
 * depend on. The two lists happen to coincide today; they are not required to.
 */
enum class AppLanguage(val code: String, val endonym: String) {
    ENGLISH("en", "English"),
    GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"),
    SPANISH("es", "Español"),
    ITALIAN("it", "Italiano"),
    PORTUGUESE("pt", "Português"),
    DUTCH("nl", "Nederlands"),
    LITHUANIAN("lt", "Lietuvių"),
    TURKISH("tr", "Türkçe"),
    AZERBAIJANI("az", "Azərbaycan"),
    POLISH("pl", "Polski"),
    UKRAINIAN("uk", "Українська"),
    RUSSIAN("ru", "Русский"),
    GREEK("el", "Ελληνικά"),
    ARABIC("ar", "العربية"),
    FARSI("fa", "فارسی"),
    JAPANESE("ja", "日本語"),
    CHINESE("zh", "中文"),
    KOREAN("ko", "한국어");

    companion object {
        /**
         * Matches on the language subtag alone, so a stored `pt-BR` or `zh-Hans-CN` still resolves
         * to its entry. Null when nothing matches — treated as "system default" by the caller.
         */
        fun fromTag(tag: String?): AppLanguage? {
            val language = tag?.substringBefore('-')?.takeIf { it.isNotBlank() } ?: return null
            return entries.firstOrNull { it.code.equals(language, ignoreCase = true) }
        }
    }
}
