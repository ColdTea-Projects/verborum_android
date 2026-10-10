package de.coldtea.verborum.bibliotheca.imported.domain.model

/** One downloaded dictionary in the user's vault; [importedAt] is null when the server omits it. */
data class VaultEntry(
    val dictionaryId: String,
    val importedAt: Long?,
)
