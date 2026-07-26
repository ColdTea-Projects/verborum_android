package de.coldtea.verborum.core.auth.domain

import android.content.Intent
import de.coldtea.verborum.core.auth.domain.usecase.EnsureUserProfileUseCase
import de.coldtea.verborum.core.auth.AuthTokenStore
import de.coldtea.verborum.core.auth.JwtDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Orchestrates the login lifecycle — the only auth entry point the UI talks to.
 *
 * On completing a login the order matters: persist tokens first (so the follow-up calls carry a
 * bearer), then create the profile, then let the feature modules run their [PostLoginHook]s (guest
 * data migration, kicking a full sync). Profile creation and the hooks are best-effort: a
 * validly-authenticated user must not be stranded on the login screen because a follow-up call
 * hiccuped — all of it is idempotent and retries on the next sync.
 */
class AuthService @Inject constructor(
    private val authManager: AuthManager,
    private val tokenStore: AuthTokenStore,
    private val ensureUserProfileUseCase: EnsureUserProfileUseCase,
    private val postLoginHooks: Set<@JvmSuppressWildcards PostLoginHook>,
) {
    fun loginIntent(): Intent = authManager.loginIntent()
    fun signUpIntent(): Intent = authManager.signUpIntent()

    /** Handles the AppAuth redirect result. Returns true once the session is established. */
    suspend fun completeLogin(responseData: Intent): Boolean {
        val tokens = authManager.exchangeCode(responseData) ?: return false
        val subject = JwtDecoder.subject(tokens.accessToken) ?: return false

        tokenStore.saveTokens(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            subject = subject,
        )

        runCatching {
            ensureUserProfileUseCase.invoke(
                subject = subject,
                email = JwtDecoder.email(tokens.idToken),
                displayName = JwtDecoder.displayName(tokens.idToken),
            )
        }
        // Each hook is isolated: one feature's failure must not skip another's.
        postLoginHooks.forEach { hook -> runCatching { hook.onLoginCompleted(subject) } }
        return true
    }

    /** Ends the Keycloak session and clears local tokens (guide §6). */
    suspend fun logout() = withContext(Dispatchers.IO) {
        // Back-channel logout is a blocking network call — keep it off the main thread.
        runCatching { authManager.endSession(tokenStore.currentRefreshToken()) }
        tokenStore.clear()
    }
}
