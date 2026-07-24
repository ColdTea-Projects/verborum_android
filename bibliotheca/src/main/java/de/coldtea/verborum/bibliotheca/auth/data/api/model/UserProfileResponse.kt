package de.coldtea.verborum.bibliotheca.auth.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ms_user profile as returned by `GET /users/{userId}`. Only presence matters to the client (a
 * 200 means the profile exists, a 404 means create it), so all fields are nullable/lenient.
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class UserProfileResponse(
    @SerialName("userId")
    val userId: String? = null,
    @SerialName("keycloakId")
    val keycloakId: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("displayName")
    val displayName: String? = null,
)
