package de.coldtea.verborum.core.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches `Authorization: Bearer <access>` to every outgoing request that does not already carry
 * one. ms_dictionary and ms_user reject anonymous calls with 401 (guide §9.1), so this runs on the
 * shared authenticated client. Requests made before login simply go out without a header.
 */
class AuthInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStore.currentAccessToken()

        val request = if (token != null && original.header("Authorization") == null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
