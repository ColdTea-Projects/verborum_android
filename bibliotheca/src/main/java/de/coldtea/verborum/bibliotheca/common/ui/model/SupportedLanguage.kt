package de.coldtea.verborum.bibliotheca.common.ui.model

import androidx.annotation.StringRes
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

enum class SupportedLanguage(val code: String, @StringRes val displayNameRes: Int) {
    ENGLISH("en", ResStrings.languageEnglish),
    GERMAN("de", ResStrings.languageGerman),
    FRENCH("fr", ResStrings.languageFrench),
    SPANISH("es", ResStrings.languageSpanish),
    ITALIAN("it", ResStrings.languageItalian),
    PORTUGUESE("pt", ResStrings.languagePortuguese),
    DUTCH("nl", ResStrings.languageDutch),
    LITHUANIAN("lt", ResStrings.languageLithuanian),
    TURKISH("tr", ResStrings.languageTurkish),
    AZERBAIJANI("az", ResStrings.languageAzerbaijani),
    POLISH("pl", ResStrings.languagePolish),
    UKRAINIAN("uk", ResStrings.languageUkrainian),
    RUSSIAN("ru", ResStrings.languageRussian),
    GREEK("el", ResStrings.languageGreek),
    ARABIC("ar", ResStrings.languageArabic),
    FARSI("fa", ResStrings.languageFarsi),
    JAPANESE("ja", ResStrings.languageJapanese),
    CHINESE("zh", ResStrings.languageChinese),
    KOREAN("ko", ResStrings.languageKorean);

    companion object {
        fun fromCode(code: String): SupportedLanguage? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}
