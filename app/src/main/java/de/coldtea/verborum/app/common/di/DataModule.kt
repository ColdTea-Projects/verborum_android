package de.coldtea.verborum.app.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.app.common.data.db.VerborumDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataModule {

    @Provides
    @Singleton
    fun provideVerborumDatabase(@ApplicationContext appContext: Context): VerborumDatabase = VerborumDatabase.getInstance(appContext)
}