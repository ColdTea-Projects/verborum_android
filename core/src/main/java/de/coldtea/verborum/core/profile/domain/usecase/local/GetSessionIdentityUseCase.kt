package de.coldtea.verborum.core.profile.domain.usecase.local

import de.coldtea.verborum.core.auth.AuthTokenStore
import de.coldtea.verborum.core.auth.JwtDecoder
import de.coldtea.verborum.core.profile.domain.model.SessionIdentity
import javax.inject.Inject

/**
 * Who is signed in, read from the stored session: the `sub` plus the email/name claims of the
 * access token (Keycloak puts them there under the `email`/`profile` scopes). Needed to create the
 * profile when login-time creation did not happen (offline, ms_user down). Null when signed out.
 */
class GetSessionIdentityUseCase @Inject constructor(
    private val tokenStore: AuthTokenStore,
) {
    fun invoke(): SessionIdentity? {
        val subject = tokenStore.currentUserId() ?: return null
        val accessToken = tokenStore.currentAccessToken()
        return SessionIdentity(
            subject = subject,
            email = JwtDecoder.email(accessToken),
            displayName = JwtDecoder.displayName(accessToken),
        )
    }
}
