package de.coldtea.verborum.core.auth.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Creates the ms_user profile row after the first login (guide §3). `userId` is ms_user's own
 * primary key and `keycloakId` is the JWT subject; the client sends the subject for both so the
 * profile resolves idempotently across re-installs (backend P3-05 will make `userId` the subject
 * anyway). `email` is unique — one profile per email.
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
    @SerialName("displayName")
    val displayName: String,
)
