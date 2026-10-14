package de.coldtea.verborum.bibliotheca.push.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.coldtea.verborum.bibliotheca.push.domain.SyncPushMessageHandler
import de.coldtea.verborum.core.push.domain.PushMessageHandler

/** Contributes bibliotheca's push handler to core's handler set. */
@InstallIn(SingletonComponent::class)
@Module
abstract class PushHandlerModule {

    @Binds
    @IntoSet
    abstract fun bindSyncPushMessageHandler(handler: SyncPushMessageHandler): PushMessageHandler
}
