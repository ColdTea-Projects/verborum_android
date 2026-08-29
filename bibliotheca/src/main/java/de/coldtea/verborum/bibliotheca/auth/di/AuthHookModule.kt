package de.coldtea.verborum.bibliotheca.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.coldtea.verborum.bibliotheca.auth.domain.BibliothecaPostLoginHook
import de.coldtea.verborum.bibliotheca.auth.domain.BibliothecaPostLogoutHook
import de.coldtea.verborum.core.auth.domain.PostLoginHook
import de.coldtea.verborum.core.auth.domain.PostLogoutHook

/** Contributes bibliotheca's login/logout lifecycle work to core's hook sets. */
@InstallIn(SingletonComponent::class)
@Module
abstract class AuthHookModule {

    @Binds
    @IntoSet
    abstract fun bindBibliothecaPostLoginHook(hook: BibliothecaPostLoginHook): PostLoginHook

    @Binds
    @IntoSet
    abstract fun bindBibliothecaPostLogoutHook(hook: BibliothecaPostLogoutHook): PostLogoutHook
}
