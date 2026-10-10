package de.coldtea.verborum.bibliotheca.imported.domain

import de.coldtea.verborum.core.library.domain.ImportedDictionariesStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Bibliotheca's downloaded dictionaries, exposed to the Forum through core's contract. */
class BibliothecaImportedDictionariesStore @Inject constructor(
    private val importedDictionaryService: ImportedDictionaryService,
) : ImportedDictionariesStore {

    override fun observeImportedDictionaryIds(): Flow<Set<String>> =
        importedDictionaryService.observeDictionaryIds()

    override suspend fun onDictionaryImported(dictionaryId: String, publisherName: String?) =
        importedDictionaryService.addImportedDictionary(dictionaryId, publisherName)
}
