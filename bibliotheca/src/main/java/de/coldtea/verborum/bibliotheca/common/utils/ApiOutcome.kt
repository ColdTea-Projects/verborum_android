package de.coldtea.verborum.bibliotheca.common.utils

import retrofit2.Response

/**
 * True only when the call reached the server and it accepted the request.
 *
 * A suspend Retrofit call returning [Response] does not report a lost connection as an
 * unsuccessful response — it throws (IOException). For the offline-safe write paths the two are
 * the same answer: the local tombstone or unsynced flag stays put and the sync upload phase
 * retries later, so a thrown network error must not surface to the user as a failed action.
 *
 * Use this only where a failure is genuinely retried by sync — never to hide an error the user
 * needs to see.
 */
suspend fun <T> succeededRemotely(call: suspend () -> Response<T>): Boolean =
    runCatching { call().isSuccessful }.getOrDefault(false)
