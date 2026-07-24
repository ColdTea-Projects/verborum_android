package de.coldtea.verborum.bibliotheca.auth.domain

import android.content.Intent
import de.coldtea.verborum.bibliotheca.auth.domain.usecase.EnsureUserProfileUseCase
import de.coldtea.verborum.bibliotheca.auth.domain.usecase.MigrateGuestDataUseCase
import de.coldtea.verborum.bibliotheca.common.domain.SyncScheduler
import de.coldtea.verborum.core.auth.AuthTokenStore
import de.coldtea.verborum.core.auth.JwtDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Orchestrates the login lifecycle — the only auth entry point the UI talks to.
 *
 * On completing a login the order matters: persist tokens first (so the follow-up calls carry a
 * bearer), then create the profile and migrate guest data, then kick a full sync. Profile creation
 * and migration are best-effort: a validly-authenticated user must not be stranded on the login
 * screen because a follow-up call hiccuped — both are idempotent and retry on the next sync.
 */
class AuthService @Inject constructor(
    private val authManager: AuthManager,
    private val tokenStore: AuthTokenStore,
    private val ensureUserProfileUseCase: EnsureUserProfileUseCase,
    private val migrateGuestDataUseCase: MigrateGuestDataUseCase,
    private val syncScheduler: SyncScheduler,
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
        runCatching { migrateGuestDataUseCase.invoke(subject) }

        // Full reconcile so migrated rows go up and any server-side data comes down.
        syncScheduler.requestImmediateSync(uploadOnly = false)
        return true
    }

    /** Ends the Keycloak session and clears local tokens (guide §6). */
    suspend fun logout() = withContext(Dispatchers.IO) {
        // Back-channel logout is a blocking network call — keep it off the main thread.
        runCatching { authManager.endSession(tokenStore.currentRefreshToken()) }
        tokenStore.clear()
    }
}
