package de.coldtea.verborum.forum.marketplace.data.api

import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplacePageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ms_marketplace (`:8087`) browse endpoints (`docs/marketplace-client-guide.md` §5.1). Members
 * only: a non-member gets 403. All filters are optional; a null or empty one is left out of the
 * query, and a list is sent as a repeated parameter (`pair=EN-TR&pair=FR-DE`).
 */
interface MarketplaceApi {
    @GET("marketplace/dictionaries")
    suspend fun getListings(
        @Query("page") page: Int,
        @Query("size") size: Int,
        // "EN-TR": two codes, either direction matches. At most 10.
        @Query("pair") pairs: List<String>,
        // Repeated, never comma-joined — the server splits on commas. At most 10.
        @Query("tag") tags: List<String>,
        // Part of the publisher's display name, 3–255 characters.
        @Query("publisher") publisher: String?,
    ): MarketplacePageResponse

    /**
     * Downloads a dictionary: adds a reference to it to the caller's vault (no body, no user id —
     * the server uses the token). 201 also on a repeat.
     */
    @POST("marketplace/dictionaries/{dictionaryId}/import")
    suspend fun importDictionary(@Path("dictionaryId") dictionaryId: String): Response<Unit>

    /** Same filters and paging; most imported first, newest first among equals. */
    @GET("marketplace/dictionaries/popular")
    suspend fun getPopularListings(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("pair") pairs: List<String>,
        @Query("tag") tags: List<String>,
        @Query("publisher") publisher: String?,
    ): MarketplacePageResponse
}
