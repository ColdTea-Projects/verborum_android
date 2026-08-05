---
title: "Authentication in Verborum Android"
subtitle: "How tokens work, and what happens on login, register, logout, and every API request"
date: "2026-08-05"
---

# The cast of characters

| Piece | Where | Job |
|---|---|---|
| **Keycloak** | external server, realm `verborum` | The identity provider. It owns passwords, sign-up, email verification, and Google/Facebook login. Our app never sees a password. |
| `AuthManager` | `core/auth/domain/` | Thin wrapper over the AppAuth library — opens the browser, exchanges the code for tokens, ends the session. |
| `AuthService` | `core/auth/domain/` | The orchestrator. The only auth entry point the UI talks to. |
| `AuthTokenStore` | `core/auth/` | The vault. Holds access + refresh token + subject in EncryptedSharedPreferences, and exposes `isLoggedIn`. |
| `AuthInterceptor` | `core/auth/` | Stamps `Authorization: Bearer …` on every outgoing API call. |
| `TokenAuthenticator` | `core/auth/` | Catches 401s and silently refreshes. |
| `JwtDecoder` | `core/auth/` | Peeks inside the tokens to read `sub`, `email`, `name` — no signature check, the servers do that. |

# The three tokens

- **Access token** — the short-lived badge (minutes). It is what we attach to every request to `ms_dictionary` / `ms_user`. It expires constantly; that is normal and by design.
- **Refresh token** — the long-lived "I'm still me" ticket. Never sent to our own backend. Its only use is going back to Keycloak and asking for a fresh access token. We explicitly request the `offline_access` scope so this one is long-lived — otherwise a phone offline for a few days would be kicked back to the login screen (`AuthConfig.kt:20`).
- **ID token** — a one-time information card about the user (email, name, `email_verified`). We read it right after login to create the user profile, then it is not used again.

Only two are stored: access + refresh, plus the `sub` — the Keycloak user id, which is what stamps every dictionary and word row as "owned by you".

# Login, step by step

1. User taps *Log in*. `AuthManager` builds an **Authorization Code + PKCE** request and hands the system a browser Intent. PKCE means the app generates a secret, sends only a hash of it, and proves it later — so even if someone intercepts the returned code, it is useless to them.
2. The browser shows **Keycloak's** page. Password typing, Google/Facebook, all of it happens there, not in our app.
3. Keycloak redirects back into the app with a short-lived **authorization code**.
4. `AuthService.completeLogin()` takes over:
   - `AuthManager.exchangeCode()` trades the code (+ the PKCE secret) for the token trio.
   - Read `sub` from the access token. No `sub` → treat as failure.
   - Check `email_verified` on the ID token. Unverified → **nothing is saved**; the user stays on the login wall with a "check your inbox" message (`LoginOutcome.EmailNotVerified`).
   - **Save tokens first** — everything after this needs a bearer header to work.
   - `EnsureUserProfileUseCase` — `GET /users/{sub}`; only on a clean 404 do we `POST` a new profile. A network error is left alone deliberately, so a flaky connection cannot create duplicates.
   - Run every `PostLoginHook`. Today bibliotheca's hook re-owns guest dictionaries under the real `sub`, then kicks a full sync. Each hook is wrapped in `runCatching` — a failing hook must never strand a genuinely logged-in user.
5. Saving the tokens flips `AuthTokenStore.isLoggedIn` to `true`, and `MainActivity` swaps the login screen for the app (`MainActivity.kt:39`). There is no explicit navigation anywhere — the state flip *is* the navigation.

# Register

There is no native registration screen. Sign-up is **the identical OAuth flow pointed at Keycloak's `/registrations` endpoint** instead of `/auth` (`AuthManager.signUpIntent()`).

> One wrinkle: after signing up, Keycloak parks the browser on its "verify your email" page and never redirects back — which from the app's side looks exactly like the user cancelling. So `LoginViewModel` remembers which button was tapped, and on a null result shows "check your inbox" rather than silently doing nothing.

# Making a request

Every call through the shared OkHttp client (`NetworkModule.kt:32`) goes:

1. `AuthInterceptor` adds `Authorization: Bearer <access token>` — unless there is no token (pre-login calls just go out bare) or one is already set.
2. If the server answers **401**, OkHttp hands the response to `TokenAuthenticator`, which:
   - gives up if this chain already retried once (no infinite loops);
   - gives up if there is no refresh token;
   - checks whether *another* parallel request already refreshed while this one waited — if the stored token differs from the one that just failed, it reuses that instead of burning a second refresh;
   - otherwise POSTs `grant_type=refresh_token` to Keycloak using a **separate bare OkHttp client** (the shared one would loop back through this same authenticator, and Keycloak's token endpoint must not receive a bearer);
   - on success, stores the new access token (and the rotated refresh token if Keycloak sent one) and **replays the original request** with the new header. The user sees nothing.
3. If the refresh is rejected (`invalid_grant` — the refresh token is genuinely dead), `tokenStore.clear()` runs, `isLoggedIn` flips false, and the app returns to the login wall.
4. If the refresh fails from *no connectivity*, the tokens are deliberately left intact — a tunnel should not log you out.

> **Key consequence:** an expired access token never logs you out. Only a dead refresh token or an explicit logout does.

# Logout

From Options (`OptionsViewModel.logout()`) → `AuthService.logout()`:

1. Back-channel POST to Keycloak's `/logout` with the refresh token, on the IO dispatcher. This kills the SSO session server-side — skipping it would leave the browser session alive, and the next "login" would silently re-authenticate the same person with no prompt.
2. `tokenStore.clear()` wipes the encrypted prefs regardless of whether step 1 succeeded.
3. `isLoggedIn` → false → the shell swaps the whole nav graph for the login screen.

# Two details worth knowing

## Startup

`isLoggedIn` is a `Boolean?`, and `null` means "still reading the encrypted store off the main thread". `MainActivity` renders *nothing* for `null` — that is what stops an already-signed-in user from seeing a flash of the login screen.

## Corrupted keystore

`AuthTokenStore` wraps its EncryptedSharedPreferences creation in a recovery path. It is a known security-crypto failure mode that otherwise throws on every launch and crash-loops the app forever; instead we delete the key alias and the prefs file, landing the user in a clean signed-out state.
