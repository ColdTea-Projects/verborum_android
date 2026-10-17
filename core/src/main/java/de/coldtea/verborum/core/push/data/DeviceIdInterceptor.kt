package de.coldtea.verborum.core.push.data

import de.coldtea.verborum.core.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adds `X-Device-Id` to every **write** the backend turns into a sync nudge, so it can skip this install
 * when it sends the account's other devices a silent `SYNC` (docs/push-notifications-client-guide.md §3):
 * dictionaries, words, tags, a Forum download, removing a download from the vault.
 *
 * Every call goes to one gateway origin (P5-09), so the scope is the method and the path: non-GET writes to
 * the API, except `notifications/` — device registration carries the id in its own body. Without the header
 * nothing breaks — this device just syncs once more after its own change.
 */
class DeviceIdInterceptor @Inject constructor(
    private val deviceIdStore: DeviceIdStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isWrite = request.method != "GET" && request.method != "HEAD"
        val isNudgingApi = request.url.toString().startsWith(API_ORIGIN) &&
            request.url.encodedPath.removePrefix("/").startsWith(NOTIFICATIONS_PATH).not()

        if (!isWrite || !isNudgingApi || request.header(HEADER) != null) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder().header(HEADER, deviceIdStore.deviceId()).build())
    }

    companion object {
        const val HEADER = "X-Device-Id"

        private val API_ORIGIN = BuildConfig.ROOT_URL_VERBORUM_API

        /** Device registration names the device in its body, so the header would only repeat it. */
        private const val NOTIFICATIONS_PATH = "notifications/"
    }
}
