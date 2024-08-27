package de.coldtea.verborum.bibliotheca.word.data.api

import de.coldtea.verborum.bibliotheca.word.data.api.model.WordBundleRequest
import de.coldtea.verborum.bibliotheca.word.data.api.model.WordResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WordApi {
    @GET("words/dictionary/{dictionaryId}")
    suspend fun getWordsByDictionary(@Path("dictionaryId") dictionaryId: String): List<WordResponse>?

    @GET("words/user/{userId}")
    suspend fun getWordsByUser(@Path("userId") userId: String): List<WordResponse>?

    @GET("words/language/from/{language}")
    suspend fun getWordsByLanguageFrom(@Path("language") language: String): List<WordResponse>?

    @GET("words/language/to/{language}")
    suspend fun getWordsByLanguageTo(@Path("language") language: String): List<WordResponse>?

    @POST("words")
    suspend fun createWords(@Body body: List<WordBundleRequest>): Response<Unit>

    @PUT("words")
    suspend fun updateWords(@Body body: List<WordBundleRequest>): Response<Unit>

    @DELETE("words/{wordId}")
    suspend fun deleteWord(@Path("wordId") wordId: String): Response<Unit>

    @DELETE("words/dictionary/{dictionaryId}")
    suspend fun deleteWordsByDictionaryId(@Path("dictionaryId") dictionaryId: String): Response<Unit>
}