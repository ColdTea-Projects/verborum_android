package de.coldtea.verborum.core.auth

import de.coldtea.verborum.core.extensions.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Authenticator
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On a 401, refreshes the access token exactly once and retries (guide §5). A failed refresh
 * (invalid_grant — the refresh token itself is dead) clears the session, which flips
 * [AuthTokenStore.isLoggedIn] to false and surfaces the login screen. A transient network error
 * leaves the tokens in place so a later call can try again.
 *
 * The refresh uses its own bare client — the shared authenticated client would loop back through
 * this authenticator, and the Keycloak token endpoint must not receive a bearer.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val config: AuthConfig,
    private val tokenStore: AuthTokenStore,
) : Authenticator {

    private val refreshClient = OkHttpClient()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Give up after one refresh attempt for a given request chain.
        if (responseCount(response) >= 2) return null
        val refreshToken = tokenStore.currentRefreshToken() ?: return null

        val newAccessToken = synchronized(this) {
            val currentAccess = tokenStore.currentAccessToken()
            val failedAccess = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            // Another request may have already refreshed while this one was queued: if the stored
            // token differs from the one that just failed, reuse it instead of refreshing again.
            if (currentAccess != null && currentAccess != failedAccess) {
                currentAccess
            } else {
                refreshTokens(refreshToken)
            }
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun refreshTokens(refreshToken: String): String? {
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", config.clientId)
            .build()
        val request = Request.Builder()
            .url(config.tokenEndpoint)
            .post(body)
            .build()

        return runCatching {
            refreshClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    // The refresh token is rejected — the session is genuinely over.
                    tokenStore.clear()
                    return null
                }
                val payload = resp.body?.string().orEmpty()
                val obj = json.parseToJsonElement(payload).jsonObject
                val newAccess = obj["access_token"]?.jsonPrimitive?.contentOrNull
                val newRefresh = obj["refresh_token"]?.jsonPrimitive?.contentOrNull
                if (newAccess == null) {
                    tokenStore.clear()
                    return null
                }
                tokenStore.updateAccessToken(newAccess, newRefresh)
                newAccess
            }
        }.getOrElse {
            // Transient failure (no connectivity): keep the tokens for a later retry.
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
