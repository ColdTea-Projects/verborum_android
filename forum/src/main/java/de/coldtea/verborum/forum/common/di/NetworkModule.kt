package de.coldtea.verborum.forum.common.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.core.BuildConfig
import de.coldtea.verborum.core.extensions.json
import de.coldtea.verborum.forum.marketplace.data.api.MarketplaceApi
import de.coldtea.verborum.forum.marketplace.data.api.SharedDictionaryApi
import de.coldtea.verborum.forum.marketplace.data.api.SharedWordApi
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * ms_marketplace lives at its own origin (`:8087`), so it gets its own Retrofit on the shared
 * authenticated [OkHttpClient] (bearer + refresh-once-on-401). The shared-dictionary reads go to
 * ms_dictionary through core's default Retrofit. The vault API is core's (ms_user).
 */
@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    @Named("marketplaceRetrofit")
    fun provideMarketplaceRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.ROOT_URL_VERBORUM_MARKETPLACE_API)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideMarketplaceApi(@Named("marketplaceRetrofit") retrofit: Retrofit): MarketplaceApi =
        retrofit.create(MarketplaceApi::class.java)

    @Provides
    @Singleton
    fun provideSharedDictionaryApi(retrofit: Retrofit): SharedDictionaryApi =
        retrofit.create(SharedDictionaryApi::class.java)

    @Provides
    @Singleton
    fun provideSharedWordApi(retrofit: Retrofit): SharedWordApi = retrofit.create(SharedWordApi::class.java)
}
