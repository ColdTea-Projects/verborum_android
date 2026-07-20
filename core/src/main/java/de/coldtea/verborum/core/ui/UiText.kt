package de.coldtea.verborum.core.ui

import android.content.Context
import androidx.annotation.StringRes

/**
 * A user-facing message a ViewModel can emit without holding a [Context]. A [Resource] is resolved
 * to a localized string in the UI layer (so error messages honour the app's locale), while
 * [Dynamic] carries an already-formed string for values that are not translatable (e.g. echoing
 * back a user's own answer).
 */
sealed interface UiText {
    data class Resource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    data class Dynamic(val value: String) : UiText

    fun resolve(context: Context): String = when (this) {
        is Resource ->
            if (args.isEmpty()) context.getString(id)
            else context.getString(id, *args.toTypedArray())
        is Dynamic -> value
    }
}
