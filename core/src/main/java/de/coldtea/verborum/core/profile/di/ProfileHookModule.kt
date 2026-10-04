package de.coldtea.verborum.core.profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.coldtea.verborum.core.auth.domain.PostLogoutHook
import de.coldtea.verborum.core.profile.domain.ProfilePostLogoutHook

/** Contributes the profile slice's logout cleanup to auth's hook set. */
@InstallIn(SingletonComponent::class)
@Module
abstract class ProfileHookModule {

    @Binds
    @IntoSet
    abstract fun bindProfilePostLogoutHook(hook: ProfilePostLogoutHook): PostLogoutHook
}
