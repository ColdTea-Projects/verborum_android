package de.coldtea.verborum.forum.marketplace.data.api

import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceWordResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * ms_dictionary's word reads for someone else's public dictionary. `level` is always null on these
 * (guide §6.8) and an unreadable dictionary answers `[]`, not 404.
 */
interface SharedWordApi {
    @GET("words/dictionary/{dictionaryId}")
    suspend fun getWords(@Path("dictionaryId") dictionaryId: String): List<MarketplaceWordResponse>
}
