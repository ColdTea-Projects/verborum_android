package de.coldtea.verborum.core.library.domain

import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages

/**
 * The signed-in user's own dictionaries, as other features need to see them.
 *
 * The dictionaries live in bibliotheca, which `core` and the other feature modules must never
 * depend on — so bibliotheca binds the implementation, and the Forum reads through this to pick
 * its default language pairs.
 */
interface OwnedDictionariesSource {

    /** One entry per own (not deleted) dictionary; empty when there are none. */
    suspend fun getOwnedDictionaryLanguages(): List<DictionaryLanguages>
}
