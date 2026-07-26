package de.coldtea.verborum.core.auth.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import de.coldtea.verborum.core.auth.domain.PostLoginHook

/**
 * Declares the [PostLoginHook] multibinding so the set resolves even when no feature module
 * contributes one (core on its own, or a test graph).
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class AuthModule {

    @Multibinds
    abstract fun postLoginHooks(): Set<PostLoginHook>
}
