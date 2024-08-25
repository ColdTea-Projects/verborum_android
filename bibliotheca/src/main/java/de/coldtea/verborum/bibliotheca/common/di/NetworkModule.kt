package de.coldtea.verborum.bibliotheca.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import retrofit2.Retrofit
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideDictionaryApi(retrofit: Retrofit): DictionaryApi = retrofit.create(DictionaryApi::class.java)

    @Provides
    @Singleton
    fun provideWordApi(retrofit: Retrofit): WordApi = retrofit.create(WordApi::class.java)

}