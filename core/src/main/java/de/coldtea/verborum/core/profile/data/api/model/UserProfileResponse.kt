package de.coldtea.verborum.core.profile.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.core.profile.domain.model.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The caller's profile as `GET /users/me` returns it (guide §4.2). */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class UserProfileResponse(
    // ms_user's userId — for /users/{userId}/vault paths. Not the token's `sub`.
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String? = null,
    // The public name shown on Forum listings; null when not set.
    @SerialName("displayName")
    val displayName: String? = null,
    // true = Forum member (guide §4.1).
    @SerialName("marketplaceAgreementAccepted")
    val marketplaceAgreementAccepted: Boolean = false,
    // The terms version last accepted; kept after a withdrawal, null if never accepted.
    @SerialName("marketplaceAgreementVersion")
    val marketplaceAgreementVersion: String? = null,
) {
    fun convertToProfile() = UserProfile(
        userId = id,
        email = email.orEmpty(),
        displayName = displayName?.takeIf(String::isNotBlank),
        isMarketplaceMember = marketplaceAgreementAccepted,
        marketplaceAgreementVersion = marketplaceAgreementVersion,
    )
}
