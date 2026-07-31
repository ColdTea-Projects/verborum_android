package de.coldtea.verborum.core.auth.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import de.coldtea.verborum.core.auth.domain.model.TokenBundle
import de.coldtea.verborum.core.BuildConfig
import de.coldtea.verborum.core.auth.AuthConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Thin wrapper over AppAuth: builds the Authorization Code + PKCE browser intents and exchanges the
 * returned code for tokens. AppAuth attaches the PKCE challenge itself (guide §2). Login and the
 * hosted sign-up form are the *same* flow pointed at different endpoints (guide §3).
 */
@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: AuthConfig,
) {
    private val authService by lazy {
        // In debug the endpoints are plain http (local Keycloak); AppAuth's default builder rejects
        // that on the token exchange, so swap in an http-permitting builder. Release stays https.
        val configuration = AppAuthConfiguration.Builder().apply {
            if (BuildConfig.DEBUG) setConnectionBuilder(InsecureConnectionBuilder)
        }.build()
        AuthorizationService(context, configuration)
    }
    private val plainClient by lazy { OkHttpClient() }

    /** Launches the standard login page. */
    fun loginIntent(): Intent = authRequestIntent(config.authorizationEndpoint)

    /** Launches Keycloak's hosted account-creation form — no native registration screen. */
    fun signUpIntent(): Intent = authRequestIntent(config.registrationEndpoint)

    private fun authRequestIntent(authEndpoint: String): Intent {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(authEndpoint),
            Uri.parse(config.tokenEndpoint),
            null, // no dynamic client registration
            Uri.parse(config.endSessionEndpoint),
        )
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            config.clientId,
            ResponseTypeValues.CODE,
            Uri.parse(config.redirectUri),
        ).setScope(config.scope).build()

        return authService.getAuthorizationRequestIntent(request)
    }

    /** Exchanges the auth code carried in the redirect result for tokens; null on error/cancel. */
    suspend fun exchangeCode(responseData: Intent): TokenBundle? {
        val response = AuthorizationResponse.fromIntent(responseData)
        if (response == null) {
            // No response means the redirect carried an error (or a stale/unmatched request) —
            // AppAuth puts the reason in the same Intent.
            logFailure("authorization", AuthorizationException.fromIntent(responseData))
            return null
        }
        val tokenResponse = performTokenRequest(response.createTokenExchangeRequest()) ?: return null
        return TokenBundle(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            idToken = tokenResponse.idToken,
        )
    }

    private suspend fun performTokenRequest(request: TokenRequest): net.openid.appauth.TokenResponse? =
        suspendCancellableCoroutine { continuation ->
            authService.performTokenRequest(request) { response, exception ->
                if (response == null) logFailure("token exchange", exception)
                continuation.resume(response)
            }
        }

    /**
     * Debug-only. [AuthorizationException] carries the OAuth error code and Keycloak's description —
     * never a token or a code — so it is safe to print, but it stays out of release builds anyway:
     * a login failure reason is diagnostic noise for users and a hint for anyone reading logcat.
     */
    private fun logFailure(stage: String, exception: AuthorizationException?) {
        if (!BuildConfig.DEBUG) return
        Log.e(
            "AuthManager",
            "Login failed at $stage: type=${exception?.type} code=${exception?.code} " +
                "error=${exception?.error} description=${exception?.errorDescription}",
        )
    }

    /**
     * Ends the Keycloak SSO session with a back-channel logout (guide §6). Dropping local tokens
     * without this leaves an SSO session behind, and the next login silently re-authenticates the
     * same user with no prompt.
     */
    fun endSession(refreshToken: String?) {
        refreshToken ?: return
        val body = FormBody.Builder()
            .add("client_id", config.clientId)
            .add("refresh_token", refreshToken)
            .build()
        val request = Request.Builder()
            .url(config.endSessionEndpoint)
            .post(body)
            .build()
        plainClient.newCall(request).execute().use { /* best-effort */ }
    }
}
