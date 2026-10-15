package de.coldtea.verborum.core.push.data

import de.coldtea.verborum.core.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adds `X-Device-Id` to every **write** the backend turns into a sync nudge, so it can skip this install
 * when it sends the account's other devices a silent `SYNC` (docs/push-notifications-client-guide.md §3):
 * ms_dictionary (dictionaries, words, tags), ms_marketplace (a Forum download) and ms_user (removing a
 * download from the vault).
 *
 * Scoped to those origins and to non-GET methods on purpose: that is the contract, and ms_notification
 * carries the id in its own body. Without the header nothing breaks — this device just syncs once more
 * after its own change.
 */
class DeviceIdInterceptor @Inject constructor(
    private val deviceIdStore: DeviceIdStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isWrite = request.method != "GET" && request.method != "HEAD"
        val url = request.url.toString()
        val isNudgingApi = NUDGING_ORIGINS.any { url.startsWith(it) }

        if (!isWrite || !isNudgingApi || request.header(HEADER) != null) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder().header(HEADER, deviceIdStore.deviceId()).build())
    }

    companion object {
        const val HEADER = "X-Device-Id"

        /** The services whose writes become a sync nudge for the account's other devices. */
        private val NUDGING_ORIGINS = listOf(
            BuildConfig.ROOT_URL_VERBORUM_API,
            BuildConfig.ROOT_URL_VERBORUM_MARKETPLACE_API,
            BuildConfig.ROOT_URL_VERBORUM_USER_API,
        )
    }
}
