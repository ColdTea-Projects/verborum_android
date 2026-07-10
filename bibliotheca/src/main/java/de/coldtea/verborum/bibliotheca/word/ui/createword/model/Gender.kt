package de.coldtea.verborum.bibliotheca.word.ui.createword.model

/**
 * Grammatical gender of a noun. Stored in the word meta as [metaCode] and rendered per language
 * either as a definite article (German der/die/das, Spanish el/la…) or, for languages without
 * articles (Lithuanian), as a short grammatical label.
 */
enum class Gender(val metaCode: String) {
    MASCULINE("m"),
    FEMININE("f"),
    NEUTER("n"),
    COMMON("c"),
}
