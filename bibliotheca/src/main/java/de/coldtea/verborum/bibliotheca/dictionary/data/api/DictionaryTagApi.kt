package de.coldtea.verborum.bibliotheca.dictionary.data.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryTagRequest
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryTagResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * The dictionary-tags sub-resource — a distinct backend controller from [DictionaryApi]
 * (docs/dictionary-tags-api.md). Same ms_dictionary origin, so it rides the shared Retrofit.
 */
interface DictionaryTagApi {
    @GET("dictionaries/{dictionaryId}/tags")
    suspend fun getDictionaryTags(
        @Path("dictionaryId") dictionaryId: String,
    ): List<DictionaryTagResponse>?

    @POST("dictionaries/{dictionaryId}/tags")
    suspend fun addDictionaryTag(
        @Path("dictionaryId") dictionaryId: String,
        @Body body: DictionaryTagRequest,
    ): Response<Unit>

    @DELETE("dictionaries/{dictionaryId}/tags/{tag}")
    suspend fun deleteDictionaryTag(
        @Path("dictionaryId") dictionaryId: String,
        @Path("tag") tag: String,
    ): Response<Unit>
}
