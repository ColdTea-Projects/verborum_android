package de.coldtea.verborum.bibliotheca.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.bibliotheca.dictionary.domain.BibliothecaOwnedDictionariesSource
import de.coldtea.verborum.core.library.domain.OwnedDictionariesSource

/** Exposes bibliotheca's dictionaries to other features through core's [OwnedDictionariesSource]. */
@InstallIn(SingletonComponent::class)
@Module
abstract class LibraryModule {

    @Binds
    abstract fun bindOwnedDictionariesSource(source: BibliothecaOwnedDictionariesSource): OwnedDictionariesSource
}
