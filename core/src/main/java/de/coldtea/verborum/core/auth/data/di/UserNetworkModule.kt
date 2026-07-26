package de.coldtea.verborum.core.auth.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.core.auth.data.api.UserApi
import de.coldtea.verborum.core.BuildConfig
import de.coldtea.verborum.core.extensions.json
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * ms_user lives at a different origin (`:8086`) than ms_dictionary, so it needs its own Retrofit —
 * reusing the shared authenticated [OkHttpClient] so profile calls carry the bearer and share the
 * 401 refresh path.
 */
@InstallIn(SingletonComponent::class)
@Module
object UserNetworkModule {

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    @Named("userRetrofit")
    fun provideUserRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.ROOT_URL_VERBORUM_USER_API)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideUserApi(@Named("userRetrofit") retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)
}
