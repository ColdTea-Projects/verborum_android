package de.coldtea.verborum.forum.vault.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One imported dictionary: a reference only, the dictionary itself stays in ms_dictionary. */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class VaultEntryResponse(
    @SerialName("vaultEntryId")
    val vaultEntryId: String? = null,
    @SerialName("dictionaryId")
    val dictionaryId: String,
    // ISO-8601 UTC.
    @SerialName("importedAt")
    val importedAt: String? = null,
)
