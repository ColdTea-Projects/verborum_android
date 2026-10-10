package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.core.extensions.apiError
import retrofit2.Response
import javax.inject.Inject

/**
 * Uploads a dictionary (`POST /dictionaries/` is an upsert) without its sharing state: the server
 * owns `isPublic` once a dictionary exists — joining or leaving the Forum flips every one of them
 * there — so a stale local value must never go up (marketplace guide §4.6).
 *
 * Only a dictionary the server does not have yet must state it, which the server answers with a
 * 400; then the local choice is sent. If that is refused because a Forum member must keep one
 * dictionary shared, it goes up shared — the member's first dictionary is their shared one.
 */
class SaveDictionaryApiUseCase @Inject constructor(
    private val dictionaryApi: DictionaryApi,
) {
    suspend fun invoke(dictionary: Dictionary): Response<Unit> {
        val update = dictionaryApi.createDictionary(dictionary.convertToRequest(isPublic = null))
        if (update.refusedWith() != IS_PUBLIC_REQUIRED) return update

        val create = dictionaryApi.createDictionary(dictionary.convertToRequest())
        if (create.refusedWith() != SHARING_REQUIRED || dictionary.isPublic) return create

        return dictionaryApi.createDictionary(dictionary.convertToRequest(isPublic = true))
    }

    /** The `error` name of a 400, or null for anything else. */
    private fun Response<Unit>.refusedWith(): String? =
        if (code() == HTTP_BAD_REQUEST) apiError()?.error else null

    private companion object {
        const val HTTP_BAD_REQUEST = 400
        // How ms_dictionary answers a new dictionary that leaves isPublic out (P4-18).
        const val IS_PUBLIC_REQUIRED = "InvalidRequestException"
        const val SHARING_REQUIRED = "SharingRequiredException"
    }
}
