package de.coldtea.verborum.core.profile.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Creates the ms_user profile after `GET /users/me` answered 404 (guide §4.3). `userId` is ms_user's
 * own key — a fresh random UUID, never reused — and `keycloakId` must be the token's `sub` (403
 * otherwise). `email` must be a valid address and is unique per profile (409 when taken).
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class UserProfileRequest(
    @SerialName("userId")
    val userId: String,
    @SerialName("keycloakId")
    val keycloakId: String,
    @SerialName("email")
    val email: String,
    // Optional; omitted from the body when null.
    @SerialName("displayName")
    val displayName: String? = null,
)
