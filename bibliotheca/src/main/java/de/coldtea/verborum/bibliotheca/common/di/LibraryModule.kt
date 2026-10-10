package de.coldtea.verborum.bibliotheca.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.bibliotheca.dictionary.domain.BibliothecaOwnedDictionariesSource
import de.coldtea.verborum.bibliotheca.imported.domain.BibliothecaImportedDictionariesStore
import de.coldtea.verborum.core.library.domain.ImportedDictionariesStore
import de.coldtea.verborum.core.library.domain.OwnedDictionariesSource

/** Exposes bibliotheca's dictionaries — own and downloaded — to other features through core's contracts. */
@InstallIn(SingletonComponent::class)
@Module
abstract class LibraryModule {

    @Binds
    abstract fun bindOwnedDictionariesSource(source: BibliothecaOwnedDictionariesSource): OwnedDictionariesSource

    @Binds
    abstract fun bindImportedDictionariesStore(store: BibliothecaImportedDictionariesStore): ImportedDictionariesStore
}
