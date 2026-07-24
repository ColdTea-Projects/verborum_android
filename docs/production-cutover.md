# Production cutover checklist — moving off localhost

What has to change on the **Android client** when the backend stops being a local-dev stack
(`localhost` / `10.0.2.2` / LAN IP over http) and becomes real servers over HTTPS. Ordered roughly
by "will break login/sync if missed" first.

Backend-side steps (Keycloak hostname pinning, SMTP, gateway, `verborum-dev-cli` removal) live with
the backend; they are flagged here only where the client depends on them. The normative auth
contract is `frontend-backend-integration.md` §6 + `client-login-guide.md`.

> **Release gate (CLAUDE.md):** nothing ships until Android + backend are ready as a pair. This
> checklist is that readiness list for the client.

---

## 1. Endpoints & HTTPS — `core/build.gradle.kts`

Five `buildConfigField`s per build type drive every network + auth call. Today **both** debug and
release point at dev hosts over **http**. For production, the `release` block must name the real
**https** origins:

| Field | Dev value (now) | Production value |
|---|---|---|
| `ROOT_URL_VERBORUM_API` (ms_dictionary) | `http://10.0.2.2:8085/` (debug) / `http://192.168.0.241:8085/` (release) | `https://<dictionary-host>/` |
| `ROOT_URL_VERBORUM_USER_API` (ms_user) | `:8086` | `https://<user-host>/` |
| `KEYCLOAK_ISSUER` | `http://localhost:8180/realms/verborum` (debug) | `https://<keycloak-host>/realms/verborum` |
| `OAUTH_CLIENT_ID` | `verborum-app` | usually unchanged (confirm the prod realm uses the same client id) |
| `OAUTH_REDIRECT_URI` | `de.coldtea.verborum://oauth2redirect/cb` | **unchanged** — a custom scheme, not host-dependent |

Notes:
- **`KEYCLOAK_ISSUER` must exactly equal the issuer the services validate.** A mismatch is the
  §7 "401 that looks like a broken token" trap. On real servers this means one canonical https
  origin everywhere (no more `localhost`-vs-`10.0.2.2` juggling).
- Once **HTTPS is in place, the AppAuth http shim (§3) and the cleartext allowance (§2) become
  dead/undesirable** — see those sections.
- **When the backend API gateway lands** (backend Phase 5, `client-login-guide.md` §9.4), the two
  per-service base URLs collapse into one gateway origin. Until then, keep both.

## 2. Network security config — `app/src/main/res/xml/network_security_config.xml`

Currently permits cleartext **globally**:

```xml
<base-config cleartextTrafficPermitted="true"> … </base-config>
```

This exists so the emulator can talk http to local Keycloak/services. **In production, remove the
global cleartext allowance** so the app refuses accidental plaintext. Either delete the file (and the
`android:networkSecurityConfig` manifest attribute) to fall back to the platform default
(cleartext blocked on API 28+), or scope cleartext to dev hosts only via a `debug-overrides` /
`domain-config` block. Do **not** ship `cleartextTrafficPermitted="true"` for real hosts.

## 3. AppAuth http shim — `bibliotheca/.../auth/domain/AuthManager.kt` + `InsecureConnectionBuilder.kt`

`InsecureConnectionBuilder` drops AppAuth's https-only check so the token exchange works against
local http Keycloak. It is already gated: it is only installed when `BuildConfig.DEBUG` is true, so
release builds use AppAuth's default (https-only) builder. **Action: none required** — just verify
the gate is intact and that no code path forces it on in release. With https Keycloak it is simply
never used.

## 4. HTTP body logging — `core/.../di/NetworkModule.kt`

`HttpLoggingInterceptor` at `BODY` level is already guarded by `BuildConfig.DEBUG` (it leaks payloads
and `Authorization` headers). **Action: none** — confirm the guard survives; never log bodies in
release.

## 5. Dev-only local steps to stop doing

- **`adb reverse tcp:8180 tcp:8180`** (and any other port forwards) — a localhost workaround only.
  Irrelevant once the client points at real hostnames.
- **`verborum-dev-cli` password-grant token trick** (`client-login-guide.md` §8) — a local-dev
  client. It must never exist in a shared/prod realm; the real client is PKCE-only. Backend-owned,
  but do not build client code that depends on it.

## 6. Keycloak realm (mostly backend, client-relevant bits)

- The mobile **redirect URI `de.coldtea.verborum://oauth2redirect/cb` must be registered on the prod
  realm's `verborum-app` client**, exactly as in dev. An unregistered URI is rejected before the
  login page renders (`client-login-guide.md` §2). Custom scheme is host-independent, so no value
  change — just confirm it is present in the production realm export.
- **PKCE S256 is enforced** server-side; AppAuth already sends it. No client change.
- **Google sign-in** stays federated *behind* Keycloak. When the backend adds real Google
  credentials, the existing flow just starts working — never integrate the Google SDK directly, and
  do not add a client-side Google button that bypasses Keycloak.
- **SMTP / password reset**: hosted flows only deliver mail once the backend has SMTP; no client
  change, but "Forgot password" and email verification are non-functional until then.

## 7. Identity contract watch-items

- We send the **JWT `sub` as ms_user's `userId` *and* `keycloakId`** (idempotent across reinstalls).
  Backend **P3-05** will make `userId` the JWT subject server-side — a coordinated breaking change.
  When it lands, re-check `EnsureUserProfileUseCase` still creates/looks up the profile correctly.
- Uploads carry `userId = the dictionary's owner = sub`. Sending anything other than your own `sub`
  is a 403 (`client-login-guide.md` §9.2). The guest UUID (`00000000-…`) must never reach the
  server — `MigrateGuestDataUseCase` rewrites it at first login. Keep that ordering (migrate before
  the first authenticated upload).
- **Different user logging in on the same device** should be treated as wipe-and-resync of the local
  store (§6). Not implemented yet — add before multi-account is a real scenario.

## 8. Release build hardening — `app/build.gradle.kts`

- **Signing:** add a real `signingConfig` for `release` with a keystore kept **out of git**
  (`local.properties` / CI secrets). Currently release is effectively debug-signed.
- **Minify/R8:** `isMinifyEnabled = false` today. Turn on R8 + shrinking for release. The
  serialization DTOs are already `@Keep @Serializable`; verify ProGuard rules cover AppAuth, Room,
  Retrofit, kotlinx-serialization, and the auth models after enabling.
- Re-test login end-to-end on a **minified release build** — reflection/serialization breakage from
  shrinking typically only shows up there.

## 9. Local database

- `BibliothecaDatabase` has **no migrations** (hard rule) — a schema change is a data-loss risk that
  needs a version bump. Before first public release, freeze the schema or add real migrations;
  after release, every entity change needs a migration path. The auth work added columns-worth of
  churn only via re-flagging (`isSynced`) and owner rewrites, not schema changes — but the token
  store is separate (EncryptedSharedPreferences), so no DB impact there.

## 10. Physical-device / staging sanity (the §7 trap)

When testing against a non-local backend, all three must name the **same** origin or every call
401s with what looks like a broken token:

```
KEYCLOAK_HOSTNAME_URL   = https://<keycloak-host>
KEYCLOAK_ISSUER_URI     = https://<keycloak-host>/realms/verborum   (each service)
KEYCLOAK_JWK_SET_URI    = https://<keycloak-host>/realms/verborum/protocol/openid-connect/certs
```

and the client's `KEYCLOAK_ISSUER` must match. A token that works in one environment but 401s in
another almost always means these disagree.

---

### Quick "flip to prod" summary

1. Set the three `release` URLs in `core/build.gradle.kts` to real **https** hosts.
2. Remove/scope the global cleartext allowance in `network_security_config.xml`.
3. Add release signing + enable R8; re-test login on the minified build.
4. Confirm the prod realm registers the `de.coldtea.verborum://oauth2redirect/cb` redirect and has
   no `verborum-dev-cli`.
5. Stop the dev-only bits (`adb reverse`, http shim is auto-off in release).
6. Ship Android + backend together.
