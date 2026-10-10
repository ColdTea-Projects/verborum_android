package de.coldtea.verborum.core.library.domain

import kotlinx.coroutines.flow.Flow

/**
 * The dictionaries the user downloaded (imported) from the Forum, as other features see them.
 *
 * Bibliotheca keeps the downloaded copies — read-only, never synced — so the user can browse and
 * practise them like their own; the Forum, which performs the import, must not depend on it. So
 * bibliotheca binds the implementation and the Forum talks to it through this.
 */
interface ImportedDictionariesStore {

    /** The ids of every downloaded dictionary on this device. */
    fun observeImportedDictionaryIds(): Flow<Set<String>>

    /**
     * A dictionary was just imported: copy it locally right away rather than waiting for the vault
     * (which the backend updates a moment later). Best-effort — a failure is repaired by the next
     * vault refresh. [publisherName] is only known from the Forum listing, so it is kept here.
     */
    suspend fun onDictionaryImported(dictionaryId: String, publisherName: String?)
}
