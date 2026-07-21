package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Total number of local rows still awaiting upload — dictionaries plus words that are unsynced or
 * tombstoned. The database is the single source of truth for "is there anything to push", so no
 * separate dirty flag is kept (which could drift from the actual rows). Emits on every relevant
 * write, driving the immediate upload trigger.
 */
class ObservePendingUploadsUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val wordRepository: WordRepository,
) {
    fun invoke(): Flow<Int> = combine(
        dictionaryRepository.observePendingUploadCount(),
        wordRepository.observePendingUploadCount(),
    ) { dictionaries, words -> dictionaries + words }
}
