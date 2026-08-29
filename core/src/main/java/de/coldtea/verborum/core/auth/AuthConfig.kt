package de.coldtea.verborum.core.auth

import de.coldtea.verborum.core.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OpenID Connect endpoints for the Keycloak `verborum` realm, derived from the build-time issuer.
 * Kept as plain string concatenation (rather than a discovery-document fetch) so login has no extra
 * network round trip; the paths are stable Keycloak conventions. See `docs/client-login-guide.md` §2.
 */
@Singleton
class AuthConfig @Inject constructor() {
    val issuer: String = BuildConfig.KEYCLOAK_ISSUER
    val clientId: String = BuildConfig.OAUTH_CLIENT_ID
    val redirectUri: String = BuildConfig.OAUTH_REDIRECT_URI

    // offline_access is requested explicitly — without it Keycloak issues no long-lived refresh
    // token and a device offline for days is forced back to the login screen (guide §2).
    val scope: String = "openid profile email offline_access"

    val authorizationEndpoint: String = "$issuer/protocol/openid-connect/auth"
    val tokenEndpoint: String = "$issuer/protocol/openid-connect/token"

    // Hosted sign-up reuses the login flow against the registrations endpoint (guide §3).
    val registrationEndpoint: String = "$issuer/protocol/openid-connect/registrations"
    val endSessionEndpoint: String = "$issuer/protocol/openid-connect/logout"
}
