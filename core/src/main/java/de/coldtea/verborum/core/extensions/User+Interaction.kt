package de.coldtea.verborum.core.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * A click handler that swallows re-fires within [waitMs] — the double-tap guard for buttons whose
 * action must run once (submit an answer, advance a question).
 *
 * The throttle state lives in [remember], so it survives recomposition — a plain closure created
 * during composition would be rebuilt (with a reset timer) on every recomposition, voiding the
 * guard exactly when it matters: right after the first tap changes state. [rememberUpdatedState]
 * keeps the throttled closure stable while always invoking the latest [action].
 */
@Composable
fun rememberDebounced(action: () -> Unit, waitMs: Long = 500L): () -> Unit {
    val latestAction by rememberUpdatedState(action)
    return remember(waitMs) {
        var lastCallTime = 0L
        {
            val now = System.currentTimeMillis()
            if (now - lastCallTime >= waitMs) {
                lastCallTime = now
                latestAction()
            }
        }
    }
}
