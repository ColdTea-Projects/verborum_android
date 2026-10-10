package de.coldtea.verborum.core.extensions

import de.coldtea.verborum.core.network.model.ApiErrorResponse
import kotlinx.serialization.ExperimentalSerializationApi
import retrofit2.Response

/**
 * The backend's error body (`{status, error, errorDetail, …}`) of a failed response, or null when it
 * is missing or not that shape. Read it to tell two errors with the same status apart (e.g. which
 * 400); branch on the status first, and never on `errorDetail` text.
 */
@OptIn(ExperimentalSerializationApi::class)
fun Response<*>.apiError(): ApiErrorResponse? =
    runCatching { json.decodeFromString<ApiErrorResponse>(errorBody()?.string().orEmpty()) }.getOrNull()
