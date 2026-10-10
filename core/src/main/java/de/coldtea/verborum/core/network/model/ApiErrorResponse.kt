package de.coldtea.verborum.core.network.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The error body every backend service returns (guide §7). Only [errorDetail] of an
 * `InvalidProfileException` is meant for people; branch on the HTTP status, never on the text.
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class ApiErrorResponse(
    @SerialName("status")
    val status: Int? = null,
    @SerialName("error")
    val error: String? = null,
    @SerialName("errorDetail")
    val errorDetail: String? = null,
)
