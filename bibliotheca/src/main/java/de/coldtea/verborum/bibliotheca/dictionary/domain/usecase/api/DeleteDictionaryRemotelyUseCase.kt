package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.DictionaryDeletion
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.RestoreDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordByDictionaryIdUseCase
import de.coldtea.verborum.core.extensions.apiError
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Deletes a dictionary on the server and settles the local rows by the answer — the one delete path
 * for the screens and the sync upload.
 *
 * Only `DELETE /dictionaries/{id}` is sent: the server deletes the words with the dictionary, in one
 * transaction, after checking the sharing rule. Deleting the words separately first would lose them
 * whenever that check then refuses the dictionary.
 *
 * - success: the dictionary and its words are deleted locally;
 * - 400 (the sharing rule, or any other refusal): a tombstone, if any, is lifted so the dictionary
 *   shows again — a refused delete must not be retried forever;
 * - unreachable or any other failure: nothing is touched here; the caller keeps or sets a tombstone.
 */
class DeleteDictionaryRemotelyUseCase @Inject constructor(
    private val deleteDictionaryApiUseCase: DeleteDictionaryApiUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val deleteWordByDictionaryIdUseCase: DeleteWordByDictionaryIdUseCase,
    private val restoreDictionaryUseCase: RestoreDictionaryUseCase,
) {
    suspend fun invoke(dictionaryId: String): DictionaryDeletion {
        val response = try {
            deleteDictionaryApiUseCase.invoke(dictionaryId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return DictionaryDeletion.PENDING
        }

        return when {
            response.isSuccessful -> {
                deleteWordByDictionaryIdUseCase.invoke(dictionaryId)
                deleteDictionaryUseCase.invoke(dictionaryId)
                DictionaryDeletion.DELETED
            }
            response.code() == HTTP_BAD_REQUEST -> {
                restoreDictionaryUseCase.invoke(dictionaryId)
                if (response.apiError()?.error == SHARING_REQUIRED) DictionaryDeletion.SHARING_REQUIRED
                else DictionaryDeletion.REFUSED
            }
            else -> DictionaryDeletion.PENDING
        }
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
        const val SHARING_REQUIRED = "SharingRequiredException"
    }
}
