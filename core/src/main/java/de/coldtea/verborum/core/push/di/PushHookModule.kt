package de.coldtea.verborum.core.push.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import de.coldtea.verborum.core.auth.domain.PostLoginHook
import de.coldtea.verborum.core.auth.domain.PreLogoutHook
import de.coldtea.verborum.core.push.data.FcmTokenSource
import de.coldtea.verborum.core.push.data.FirebaseFcmTokenSource
import de.coldtea.verborum.core.push.domain.PushMessageHandler
import de.coldtea.verborum.core.push.domain.PushPostLoginHook
import de.coldtea.verborum.core.push.domain.PushPreLogoutHook

/** Contributes push's login/logout work to auth's hook sets, and declares the handler set features fill. */
@InstallIn(SingletonComponent::class)
@Module
abstract class PushHookModule {

    @Binds
    @IntoSet
    abstract fun bindPushPostLoginHook(hook: PushPostLoginHook): PostLoginHook

    @Binds
    @IntoSet
    abstract fun bindPushPreLogoutHook(hook: PushPreLogoutHook): PreLogoutHook

    @Binds
    abstract fun bindFcmTokenSource(source: FirebaseFcmTokenSource): FcmTokenSource

    /** Declared so the set resolves even when no feature module contributes a handler. */
    @Multibinds
    abstract fun pushMessageHandlers(): Set<PushMessageHandler>
}
