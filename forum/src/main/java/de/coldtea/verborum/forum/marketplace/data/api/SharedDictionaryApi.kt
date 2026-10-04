package de.coldtea.verborum.forum.marketplace.data.api

import de.coldtea.verborum.forum.marketplace.data.api.model.SharedDictionaryResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.SharedDictionaryTagResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ms_dictionary's dictionary reads, as the Forum uses them: someone else's **public** dictionary
 * (integration doc §3). Read-only — every write to it is a 403. A dictionary made private or
 * deleted answers 404.
 */
interface SharedDictionaryApi {
    @GET("dictionaries/dictionary/{dictionaryId}")
    suspend fun getDictionary(@Path("dictionaryId") dictionaryId: String): SharedDictionaryResponse

    /** Several at once; ids that are not readable are silently left out. */
    @GET("dictionaries/batch")
    suspend fun getDictionaries(@Query("ids") commaSeparatedIds: String): List<SharedDictionaryResponse>

    @GET("dictionaries/{dictionaryId}/tags")
    suspend fun getTags(@Path("dictionaryId") dictionaryId: String): List<SharedDictionaryTagResponse>
}
