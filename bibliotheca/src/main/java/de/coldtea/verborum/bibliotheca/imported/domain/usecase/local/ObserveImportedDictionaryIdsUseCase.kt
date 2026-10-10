package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveImportedDictionaryIdsUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    fun invoke(): Flow<Set<String>> = repository.observeDictionaryIds().map { it.toSet() }
}
