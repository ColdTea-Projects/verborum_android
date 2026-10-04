package de.coldtea.verborum.forum.marketplace.domain.usecase.local

import de.coldtea.verborum.core.library.domain.OwnedDictionariesSource
import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import javax.inject.Inject

/** The languages of the user's own Bibliotheca dictionaries, read from the local database. */
class GetOwnedDictionaryLanguagesUseCase @Inject constructor(
    private val ownedDictionariesSource: OwnedDictionariesSource,
) {
    suspend operator fun invoke(): List<DictionaryLanguages> = ownedDictionariesSource.getOwnedDictionaryLanguages()
}
