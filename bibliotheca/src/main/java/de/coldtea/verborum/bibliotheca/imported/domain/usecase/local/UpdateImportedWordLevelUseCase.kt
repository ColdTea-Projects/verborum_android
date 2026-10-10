package de.coldtea.verborum.bibliotheca.imported.domain.usecase.local

import de.coldtea.verborum.bibliotheca.imported.data.ImportedDictionaryRepository
import javax.inject.Inject

/** Practice progress on a downloaded word — local only, never uploaded (guide §6.8). */
class UpdateImportedWordLevelUseCase @Inject constructor(
    private val repository: ImportedDictionaryRepository,
) {
    suspend fun invoke(wordId: String, level: Int) = repository.updateWordLevel(wordId, level)
}
