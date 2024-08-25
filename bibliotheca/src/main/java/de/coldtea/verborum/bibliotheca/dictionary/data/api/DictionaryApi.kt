package de.coldtea.verborum.bibliotheca.dictionary.data.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryRequest
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DictionaryApi {
    @GET("dictionaries/{userId}")
    suspend fun getAllDictionariesByUser(@Path("userId") userId: String): List<DictionaryResponse>?

    @POST("dictionaries/")
    suspend fun createDictionary(@Body body: DictionaryRequest): Response<Unit>

    @PUT("dictionaries/")
    suspend fun updateDictionary(@Body body: DictionaryRequest): Response<Unit>

    @DELETE("dictionaries/{dictionaryId}")
    suspend fun deleteDictionary(@Path("dictionaryId") dictionaryId: String): Response<Unit>
}