package de.coldtea.verborum.core.push.data

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** The FCM registration token, behind an interface so the registration logic is testable on the JVM. */
interface FcmTokenSource {

    /** The current token, or null when FCM could not provide one (offline, Play services missing). */
    suspend fun currentToken(): String?

    /** Invalidates the token on the device; the server drops its row when FCM next reports it dead. */
    suspend fun deleteToken()
}

@Singleton
class FirebaseFcmTokenSource @Inject constructor() : FcmTokenSource {

    override suspend fun currentToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            continuation.resume(if (task.isSuccessful) task.result else null)
        }
    }

    override suspend fun deleteToken() = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { continuation.resume(Unit) }
    }
}
