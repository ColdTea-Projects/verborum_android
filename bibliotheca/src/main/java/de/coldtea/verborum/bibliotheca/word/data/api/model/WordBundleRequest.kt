package de.coldtea.verborum.bibliotheca.word.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class WordBundleRequest(
    @SerialName("dictionaryId")
    val dictionaryId: String,
    @SerialName("words")
    val words: List<WordRequest>,
)