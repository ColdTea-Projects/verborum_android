package de.coldtea.verborum.bibliotheca.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataModule {

    @Provides
    @Singleton
    fun provideVerborumDatabase(@ApplicationContext appContext: Context): BibliothecaDatabase = BibliothecaDatabase.getInstance(appContext)
}