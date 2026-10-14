package de.coldtea.verborum.core.push.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.coldtea.verborum.core.auth.AuthTokenStore
import de.coldtea.verborum.core.push.data.DeviceIdStore
import de.coldtea.verborum.core.push.data.FcmTokenSource
import de.coldtea.verborum.core.push.data.api.NotificationApi
import de.coldtea.verborum.core.push.data.api.model.DeviceRegistrationRequest
import java.util.Locale
import javax.inject.Inject

/** What a registration attempt came to — [PushRegistrationWorker] maps it to success or retry. */
sealed interface PushRegistrationResult {
    /** The server has this install's token. */
    data object Registered : PushRegistrationResult

    /** Nothing to do right now: signed out, or FCM has no token yet (it calls `onNewToken` later). */
    data object Skipped : PushRegistrationResult

    /** Transient (offline, 5xx, 409): worth trying again. */
    data object Retry : PushRegistrationResult

    /** The server refused it (4xx that a retry cannot change); the next launch tries again anyway. */
    data object Rejected : PushRegistrationResult
}

/**
 * Registers this install's FCM token with ms_notification and removes it again on logout
 * (docs/push-notifications-client-guide.md §1–2). Silent pushes are an optimisation, so every outcome
 * here is best-effort and none of it may ever block or fail the user.
 */
class PushRegistrationService @Inject constructor(
    private val api: NotificationApi,
    private val deviceIdStore: DeviceIdStore,
    private val fcmTokenSource: FcmTokenSource,
    private val tokenStore: AuthTokenStore,
    @ApplicationContext private val context: Context,
) {

    suspend fun register(): PushRegistrationResult {
        if (tokenStore.currentUserId() == null) return PushRegistrationResult.Skipped
        val token = fcmTokenSource.currentToken() ?: return PushRegistrationResult.Skipped

        val first = send(deviceIdStore.deviceId(), token) ?: return PushRegistrationResult.Retry
        if (first != FORBIDDEN) return first.toResult()

        // 403: this device id is held for another account under a token this install does not have.
        // The only way forward is a new identity for the install, then one more try
        val second = send(deviceIdStore.rotate(), token) ?: return PushRegistrationResult.Retry
        return second.toResult()
    }

    /** Called before the session is torn down, while the bearer token is still valid. */
    suspend fun unregister() {
        runCatching { api.removeDevice(deviceIdStore.deviceId()) }
        // Even if the call failed the old token is now dead, so the server drops the row on its next push
        runCatching { fcmTokenSource.deleteToken() }
    }

    /** The HTTP status, or null when the call itself failed (offline, timeout). */
    private suspend fun send(deviceId: String, token: String): Int? = runCatching {
        api.registerDevice(
            DeviceRegistrationRequest(
                deviceId = deviceId,
                token = token,
                platform = PLATFORM_ANDROID,
                locale = Locale.getDefault().toLanguageTag(),
                appVersion = appVersion(),
            ),
        ).code()
    }.getOrNull()

    private fun Int.toResult(): PushRegistrationResult = when {
        this in 200..299 -> PushRegistrationResult.Registered
        this == CONFLICT || this >= 500 -> PushRegistrationResult.Retry
        else -> PushRegistrationResult.Rejected
    }

    private fun appVersion(): String? = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull()

    private companion object {
        const val PLATFORM_ANDROID = "ANDROID"
        const val FORBIDDEN = 403
        const val CONFLICT = 409
    }
}
