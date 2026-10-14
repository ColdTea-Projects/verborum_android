package de.coldtea.verborum.core.push.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.core.BuildConfig
import de.coldtea.verborum.core.extensions.json
import de.coldtea.verborum.core.push.data.api.NotificationApi
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * ms_notification lives at its own origin (`:8088`), so like ms_user it gets its own Retrofit over
 * the shared authenticated [OkHttpClient] — the bearer and the 401 refresh path come for free.
 */
@InstallIn(SingletonComponent::class)
@Module
object PushNetworkModule {

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    @Named("notificationRetrofit")
    fun provideNotificationRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.ROOT_URL_VERBORUM_NOTIFICATION_API)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideNotificationApi(@Named("notificationRetrofit") retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)
}
