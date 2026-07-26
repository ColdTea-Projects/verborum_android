package de.coldtea.verborum.bibliotheca.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.coldtea.verborum.bibliotheca.auth.domain.BibliothecaPostLoginHook
import de.coldtea.verborum.core.auth.domain.PostLoginHook

/** Contributes bibliotheca's post-login work to core's [PostLoginHook] set. */
@InstallIn(SingletonComponent::class)
@Module
abstract class AuthHookModule {

    @Binds
    @IntoSet
    abstract fun bindBibliothecaPostLoginHook(hook: BibliothecaPostLoginHook): PostLoginHook
}
