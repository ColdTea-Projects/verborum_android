package de.coldtea.verborum.bibliotheca.word.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class WordRequest(
    @SerialName("wordId")
    val wordId: String,
    @SerialName("dictionaryId")
    val dictionaryId: String,
    @SerialName("word")
    val word: String,
    @SerialName("wordMeta")
    val wordMeta: String,
    @SerialName("translation")
    val translation: String,
    @SerialName("translationMeta")
    val translationMeta: String,
)