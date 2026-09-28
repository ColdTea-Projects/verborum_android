package de.coldtea.verborum.core.ui.components

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * "3 days ago", "5 minutes ago", … localized by the platform; recomputed only when [epochMillis]
 * changes. Shared by the bibliotheca and forum dictionary cards.
 */
@Composable
fun relativeTimeAgo(epochMillis: Long): String =
    remember(epochMillis) {
        DateUtils.getRelativeTimeSpanString(
            epochMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        ).toString()
    }
