package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryTagApi
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryTagRequest
import javax.inject.Inject

/**
 * Reconciles a dictionary's tags against the backend tag sub-resource
 * (docs/dictionary-tags-api.md). The dictionary itself must already exist on the server.
 */
class SyncDictionaryTagsUseCase @Inject constructor(
    private val dictionaryTagApi: DictionaryTagApi,
) {
    /**
     * Makes the server's tag set match [localCodes]: adds the ones it is missing and deletes the
     * ones it has that we no longer do. Returns true only when the whole reconcile succeeded — the
     * caller keeps the dictionary unsynced on false so it retries, rather than letting a later
     * download overwrite the local tags from a stale server set.
     */
    suspend fun push(dictionaryId: String, localCodes: List<String>): Boolean {
        val remote = pull(dictionaryId) ?: return false
        val remoteSet = remote.toSet()
        val localSet = localCodes.toSet()

        return runCatching {
            val added = (localSet - remoteSet).all {
                dictionaryTagApi.addDictionaryTag(dictionaryId, DictionaryTagRequest(it)).isSuccessful
            }
            val removed = (remoteSet - localSet).all {
                dictionaryTagApi.deleteDictionaryTag(dictionaryId, it).isSuccessful
            }
            added && removed
        }.getOrDefault(false)
    }

    /** The server's current tag codes for the dictionary, or null when the fetch fails. */
    suspend fun pull(dictionaryId: String): List<String>? =
        runCatching { dictionaryTagApi.getDictionaryTags(dictionaryId) }
            .getOrNull()
            ?.mapNotNull { it.tag }
}
