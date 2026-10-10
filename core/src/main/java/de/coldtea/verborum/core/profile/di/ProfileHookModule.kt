package de.coldtea.verborum.core.profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import de.coldtea.verborum.core.profile.domain.MarketplaceMembershipHook
import de.coldtea.verborum.core.auth.domain.PostLogoutHook
import de.coldtea.verborum.core.profile.domain.ProfilePostLogoutHook

/** Contributes the profile slice's logout cleanup to auth's hook set, and declares its own hooks. */
@InstallIn(SingletonComponent::class)
@Module
abstract class ProfileHookModule {

    @Binds
    @IntoSet
    abstract fun bindProfilePostLogoutHook(hook: ProfilePostLogoutHook): PostLogoutHook

    /** Declared so the set resolves even when no feature module contributes a hook. */
    @Multibinds
    abstract fun marketplaceMembershipHooks(): Set<MarketplaceMembershipHook>
}
