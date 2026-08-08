---
name: android-app-sec
description: Review and implement application security in the Verborum Android app — OAuth/OIDC via Keycloak (Authorization Code + PKCE), token storage and refresh, secrets handling, safe logging, network-security config and cleartext, owner-keyed data, and release hardening. Use when touching auth, tokens, the network layer, logging, or release configuration, and as the security lens during code review.
---

# App Security (Verborum)

The auth layer already follows these rules — preserve them, and apply the same standard to new code. Cross-refs: `docs/client-login-guide.md` (auth contract) and `docs/production-cutover.md` (go-live hardening).

## Quick start — the two shapes that matter most

```kotlin
// Verbose/network logging and dev-only affordances stay behind the debug guard.
if (BuildConfig.DEBUG) {
    addInterceptor(HttpLoggingInterceptor().apply { level = Level.BODY })
}

// Tokens live only in EncryptedSharedPreferences (Keystore-backed) — never plain prefs,
// never a log line, never a file.
EncryptedSharedPreferences.create(
    context,
    AUTH_PREFS,
    MasterKey.Builder(context).setKeyScheme(KeyScheme.AES256_GCM).build(),
    PrefKeyEncryptionScheme.AES256_SIV,
    PrefValueEncryptionScheme.AES256_GCM,
)
```

## Authentication (OAuth2 / OIDC)

- Login is **Authorization Code + PKCE (S256)** against Keycloak, driven by **AppAuth** through a Custom Tab — never a native/WebView credential form, never the password grant in shipped code (`verborum-dev-cli` is local-dev only). PKCE is enforced server-side; don't hand-roll the flow.
- Federated sign-in (Google) stays **behind Keycloak** — never integrate a third-party auth SDK directly.
- Identity comes from the token: the JWT `sub` is the row owner sent to the backend. The client `JwtDecoder` reads claims **without verifying the signature** — treat decoded claims as display/routing hints only; the services do real validation. Never make a security decision on an unverified claim.

## Token handling

- Store tokens **only** in `EncryptedSharedPreferences` backed by the Android Keystore (`AuthTokenStore`) — never plain `SharedPreferences`, files, logs, or memory that outlives need.
- Send `Authorization: Bearer <access>` via the shared `AuthInterceptor`. On 401, `TokenAuthenticator` refreshes **once**, retries, and on a dead refresh token clears the session (→ login wall); a transient network failure keeps tokens for retry. Don't add ad-hoc token plumbing around this.
- Request `offline_access` for the long-lived refresh token. **Logout must call Keycloak's end-session endpoint**, not just drop local tokens (a leftover SSO session silently re-logs-in the next user).
- On a *different* user signing in on the same device, treat the local store as wipe-and-resync (owner-keyed data must never mix).

## Secrets & logging

- **No secrets in the repo or in code** — no API keys, client secrets (the mobile client is public/PKCE, so there is none to embed), keystores, or `local.properties` committed.
- **Never log tokens, `Authorization` headers, request/response bodies, or PII in release.** The OkHttp `BODY` logging interceptor is guarded by `BuildConfig.DEBUG` — keep every verbose/network log behind that guard. Don't `Log.d` a bearer or a full DTO.
- Guard any dev-only affordance (the http-permitting `InsecureConnectionBuilder` for local Keycloak) behind `BuildConfig.DEBUG` so it can never reach release.

## Network security

- Production traffic is **HTTPS only**. The global cleartext allowance in `network_security_config.xml` is a **local-dev convenience** and must be removed/scoped before release (see production-cutover). Never ship `cleartextTrafficPermitted="true"` for real hosts.
- The Keycloak issuer in the token must match what services validate — a mismatch reads as a 401 "broken token" (see the login guide §7). Keep issuer/base-URL config consistent per environment via `BuildConfig`.
- Respect backend ownership rules: send **your own** `sub` as owner; the guest UUID (`00000000-…`) must never reach the server (rewritten at first login).

## Data & release hardening

- Room holds only the user's own rows (owner-keyed); don't broaden queries to cross owners.
- Validate/normalize external input at the boundary (e.g. `level` clamped 0–7, tags normalized) before persisting or uploading.
- For release: real signing config with the keystore kept out of git; enable R8/shrinking and re-test auth on the minified build (serialization/reflection breakage shows up only there); `@Keep @Serializable` DTOs and AppAuth/Room/Retrofit ProGuard rules verified.

## Review lens

When reviewing, flag: tokens/secrets/PII in logs or code, unguarded debug shims, plaintext token storage, `!!`/unverified-claim trust, cleartext to non-dev hosts, missing end-session on logout, and any owner-id that isn't the authenticated `sub`.
