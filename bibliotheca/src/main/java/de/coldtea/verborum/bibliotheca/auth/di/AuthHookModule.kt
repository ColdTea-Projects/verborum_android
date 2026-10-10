package de.coldtea.verborum.bibliotheca.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.coldtea.verborum.bibliotheca.auth.domain.BibliothecaMembershipHook
import de.coldtea.verborum.bibliotheca.auth.domain.BibliothecaPostLoginHook
import de.coldtea.verborum.bibliotheca.auth.domain.BibliothecaPostLogoutHook
import de.coldtea.verborum.core.auth.domain.PostLoginHook
import de.coldtea.verborum.core.auth.domain.PostLogoutHook
import de.coldtea.verborum.core.profile.domain.MarketplaceMembershipHook

/** Contributes bibliotheca's login/logout and Forum-membership work to core's hook sets. */
@InstallIn(SingletonComponent::class)
@Module
abstract class AuthHookModule {

    @Binds
    @IntoSet
    abstract fun bindBibliothecaPostLoginHook(hook: BibliothecaPostLoginHook): PostLoginHook

    @Binds
    @IntoSet
    abstract fun bindBibliothecaPostLogoutHook(hook: BibliothecaPostLogoutHook): PostLogoutHook

    @Binds
    @IntoSet
    abstract fun bindBibliothecaMembershipHook(hook: BibliothecaMembershipHook): MarketplaceMembershipHook
}
