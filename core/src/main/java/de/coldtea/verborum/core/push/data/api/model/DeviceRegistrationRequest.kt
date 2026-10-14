package de.coldtea.verborum.core.push.data.api.model

import kotlinx.serialization.Serializable

/**
 * `PUT /notifications/devices` (docs/push-notifications-client-guide.md §1). The owner is the bearer
 * token's subject — nothing about the user is in the body. [platform] is `ANDROID` or `IOS`; the
 * server answers 400 to anything else (web does not use FCM).
 */
@Serializable
data class DeviceRegistrationRequest(
    val deviceId: String,
    val token: String,
    val platform: String,
    val locale: String? = null,
    val appVersion: String? = null,
)
