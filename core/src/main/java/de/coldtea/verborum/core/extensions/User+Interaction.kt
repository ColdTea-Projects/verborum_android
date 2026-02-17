package de.coldtea.verborum.core.extensions

fun debounce(action: () -> Unit, waitMs: Long = 500L): () -> Unit {
    var lastCallTime = 0L

    return {
        val now = System.currentTimeMillis()
        if (now - lastCallTime >= waitMs) {
            lastCallTime = now
            action()
        }
    }
}

// With parameter
fun <T> debounce(action: (T) -> Unit, waitMs: Long = 500L): (T) -> Unit {
    var lastCallTime = 0L

    return { param ->
        val now = System.currentTimeMillis()
        if (now - lastCallTime >= waitMs) {
            lastCallTime = now
            action(param)
        }
    }
}