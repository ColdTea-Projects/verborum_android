package de.coldtea.verborum.core.profile.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of `PUT /users/me/profile-info` (guide §4.4). Every field is optional and a null one is left
 * out of the body (the shared `Json` has `explicitNulls = false`), which the server reads as
 * "unchanged". `displayName = ""` removes the name; [marketplaceAgreementVersion] is required
 * whenever [marketplaceAgreementAccepted] is `true` for a non-member.
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class ProfileInfoRequest(
    @SerialName("displayName")
    val displayName: String? = null,
    @SerialName("marketplaceAgreementAccepted")
    val marketplaceAgreementAccepted: Boolean? = null,
    @SerialName("marketplaceAgreementVersion")
    val marketplaceAgreementVersion: String? = null,
)
