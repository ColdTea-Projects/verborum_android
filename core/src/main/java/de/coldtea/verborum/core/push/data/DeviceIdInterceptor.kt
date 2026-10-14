package de.coldtea.verborum.core.push.data

import de.coldtea.verborum.core.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adds `X-Device-Id` to every **write** to ms_dictionary, so the backend can skip this install when it
 * sends the account's other devices a silent `SYNC` (docs/push-notifications-client-guide.md §3).
 *
 * Scoped to ms_dictionary's origin and to non-GET methods on purpose: that is the contract, and no
 * other service has any use for the id. Without the header nothing breaks — this device just syncs once
 * more after its own change.
 */
class DeviceIdInterceptor @Inject constructor(
    private val deviceIdStore: DeviceIdStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isWrite = request.method != "GET" && request.method != "HEAD"
        val isDictionaryApi = request.url.toString().startsWith(BuildConfig.ROOT_URL_VERBORUM_API)

        if (!isWrite || !isDictionaryApi || request.header(HEADER) != null) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder().header(HEADER, deviceIdStore.deviceId()).build())
    }

    companion object {
        const val HEADER = "X-Device-Id"
    }
}
