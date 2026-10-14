package de.coldtea.verborum.core.push.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.core.push.domain.PushMessageRouter
import de.coldtea.verborum.core.push.domain.PushRegistrationScheduler

/**
 * FCM's entry into the app. Both pushes this app receives are **silent data messages** — nothing is
 * shown, so no notification permission or channel exists. This class does no work itself: a new token is
 * handed to the scheduler, a message to the router, and each of those only schedules.
 *
 * Reached through a Hilt entry point rather than `@AndroidEntryPoint`: `core` is a library module
 * without the Hilt Gradle plugin, which `@AndroidEntryPoint` would require.
 */
class VerborumMessagingService : FirebaseMessagingService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PushEntryPoint {
        fun pushMessageRouter(): PushMessageRouter
        fun pushRegistrationScheduler(): PushRegistrationScheduler
    }

    private val entryPoint: PushEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, PushEntryPoint::class.java)
    }

    override fun onNewToken(token: String) {
        // The worker reads the current token itself; a signed-out app skips the registration
        entryPoint.pushRegistrationScheduler().requestRegistration()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        entryPoint.pushMessageRouter().route(message.data)
    }
}
