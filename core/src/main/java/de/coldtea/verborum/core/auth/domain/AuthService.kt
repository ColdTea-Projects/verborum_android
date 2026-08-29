package de.coldtea.verborum.core.auth.domain

import android.content.Intent
import de.coldtea.verborum.core.auth.domain.usecase.EnsureUserProfileUseCase
import de.coldtea.verborum.core.auth.AuthTokenStore
import de.coldtea.verborum.core.auth.JwtDecoder
import de.coldtea.verborum.core.auth.domain.model.LoginOutcome
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
    private val postLogoutHooks: Set<@JvmSuppressWildcards PostLogoutHook>,
) {
    fun loginIntent(): Intent = authManager.loginIntent()
    fun signUpIntent(): Intent = authManager.signUpIntent()

    /** Handles the AppAuth redirect result. [LoginOutcome.Success] once the session is established. */
    suspend fun completeLogin(responseData: Intent): LoginOutcome {
        val tokens = authManager.exchangeCode(responseData) ?: return LoginOutcome.Failed
        val subject = JwtDecoder.subject(tokens.accessToken) ?: return LoginOutcome.Failed

        // A session only exists once a refresh token is on file (AuthTokenStore.hasSession), and
        // saveTokens skips a null one — reporting Success here would strand the user in a silent
        // login loop. A realm that withholds refresh tokens is a misconfiguration; fail visibly.
        val refreshToken = tokens.refreshToken ?: return LoginOutcome.Failed

        // Keycloak normally withholds tokens until the address is confirmed; this is the belt-and-
        // braces check for realms/IdPs that hand them out anyway. Nothing is persisted, so the user
        // stays on the login wall with a "check your inbox" message instead of a half-live session.
        if (!JwtDecoder.emailVerified(tokens.idToken)) return LoginOutcome.EmailNotVerified

        tokenStore.saveTokens(
            accessToken = tokens.accessToken,
            refreshToken = refreshToken,
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
        return LoginOutcome.Success
    }

    /**
     * Ends the Keycloak session, clears local tokens (guide §6), then lets the feature modules run
     * their [PostLogoutHook]s — the local data wipe that keeps one account's rows from ever being
     * seen or synced by the next account on this device.
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        // Back-channel logout is a blocking network call — keep it off the main thread.
        runCatching { authManager.endSession(tokenStore.currentRefreshToken()) }
        tokenStore.clear()
        // Each hook is isolated: one feature's failure must not skip another's cleanup.
        postLogoutHooks.forEach { hook -> runCatching { hook.onLoggedOut() } }
    }
}
