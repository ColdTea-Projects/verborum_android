package de.coldtea.verborum.bibliotheca.imported.domain.model

/** A downloaded dictionary together with its words, as fetched from the owner's current version. */
data class ImportedDictionaryContent(
    val dictionary: ImportedDictionary,
    val words: List<ImportedWord>,
)
