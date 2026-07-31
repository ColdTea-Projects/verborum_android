# Verborum — Run the Backend Locally & Enable Google/Facebook Login

A practical setup guide for the Android developer. By the end you'll have the full backend running on your machine, be able to sign in through the branded Verborum login page (email/password, an emailed code, Google, or Facebook), and know exactly what the app talks to.

Auth is Keycloak-hosted. The app never embeds a Google/Facebook SDK — it opens the hosted Verborum login page (Authorization Code + PKCE), and the social buttons appear there automatically.

So on the client side, social login is zero extra code — you just point the app at Keycloak.

## 1. Prerequisites

- **Docker Desktop** — installed and running.
- **JDK 17** — the Spring services build on Java 17 (Boot 3.2.2).
- **Git** + the `verborum_ms` repository.
- **A `.env` file** — the project owner sends you the Google/Facebook credentials separately (never in the repo). See §3.

## 2. Run the backend

The infrastructure (Keycloak, databases, RabbitMQ, a fake mail server) runs in Docker; the two Spring services run on your host.

### 2.1 Start the infrastructure

From the repo root:

```bash
docker compose up -d
docker compose ps   # wait until keycloak reports "healthy"
```

First run builds a custom Keycloak image (it bakes in the passwordless email-code plugin), so the very first `up` takes a few minutes. Later runs are instant.

What comes up:

| Service | URL / Port | Login | What it is |
|---|---|---|---|
| Keycloak | http://localhost:8180 | admin / admin | Identity server, realm `verborum` |
| Mailpit | http://localhost:8025 | — | Fake inbox — catches all verification / sign-in-code emails |
| Postgres (dictionary) | 5432 | coldtea / qwerty | ms_dictionary DB |
| Postgres (profile) | 5433 | coldtea / qwerty | ms_user DB |
| RabbitMQ | 15672 (UI) | verborum / verborum | Event bus |
| Adminer | http://localhost:8080 | — | Web DB browser |

### 2.2 Start the two backend services

They run on the host (not in Docker). Set `JAVA_HOME` to your JDK 17, then:

```bash
# Git Bash equivalent
export JAVA_HOME="$USERPROFILE/.jdks/ms-17.0.18"
./mvnw -pl ms_dictionary spring-boot:run   # http://localhost:8085
./mvnw -pl ms_user spring-boot:run         # http://localhost:8086
```

```powershell
# PowerShell — adjust the path to your JDK 17
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\ms-17.0.18"
.\mvnw.cmd -pl ms_dictionary spring-boot:run   # http://localhost:8085
.\mvnw.cmd -pl ms_user spring-boot:run         # http://localhost:8086
```

Every host/port/credential defaults sensibly, so nothing else needs setting for a normal local run.

### 2.3 The one gotcha — resetting Keycloak

Keycloak imports its realm only on the first start of an empty data volume. If the realm ever looks stale or you pull realm changes, reset it:

```bash
docker compose down
docker volume rm verborum_ms_keycloak_data
docker compose up -d
```

## 3. The `.env` file (Google/Facebook secrets)

The owner sends you four values. Create a file named `.env` in the repo root (it's git-ignored — never commit it):

```env
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
FACEBOOK_CLIENT_ID=...
FACEBOOK_CLIENT_SECRET=...
```

Then bring the stack up (or restart the bootstrap): the Google/Facebook buttons appear on the login page automatically. Without this file, email/password and email-code login still work — only the social buttons are hidden.

The same client works on your machine unchanged, because you also run Keycloak on `localhost:8180`.

## 4. Try the login page

Open this in a browser (forces the login screen):

```
http://localhost:8180/realms/verborum/protocol/openid-connect/auth?client_id=verborum-app&response_type=code&scope=openid&prompt=login&redirect_uri=http://localhost:3000/cb&code_challenge=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRS&code_challenge_method=S256
```

After a successful login it redirects to `http://localhost:3000/cb?...&code=...`. Nothing runs on port 3000, so you'll see a "can't reach this page" error — that's success (the auth code is in the URL; in the real app, AppAuth handles that redirect).

### Ways to sign in

| Method | Works out of the box? | Notes |
|---|---|---|
| Email + password | ✅ Yes | Use the test users below |
| Email code (passwordless) | ✅ Yes | "Try another way" → "Email me a sign-in code" → read the code in Mailpit (http://localhost:8025) |
| Google | ⚠️ Needs your account allow-listed | See §5 |
| Facebook | ⚠️ Needs your account added as a tester | See §6 |

### Test users (email/password)

| Username | Password | Roles |
|---|---|---|
| testuser | testuser | user |
| testadmin | testadmin | user, admin |

New sign-ups must verify their email before they can log in — the verification link lands in Mailpit (http://localhost:8025), not a real inbox.

### Get a token from the command line (for API testing)

```bash
curl -s -X POST http://localhost:8180/realms/verborum/protocol/openid-connect/token \
  -d client_id=verborum-dev-cli \
  -d username=testuser -d password=testuser -d grant_type=password
```

## 5. Enable Google login for your account

Google only lets allow-listed test users finish the login while the app is in "Testing" mode. Sharing the client secret is not enough — your specific Google account must be added.

Ask the project owner to do this (or, if you have console access to the Verborum Google Cloud project):

1. Go to https://console.cloud.google.com and select the Verborum project (top bar).
2. Left menu → Google Auth Platform → Audience.
3. Under Test users, click **+ Add users**.
4. Enter your Google email address → Save.

That's it — no invite to accept. Reload the login page and "Continue with Google" will work for that account. (If your account isn't added, Google shows an "access blocked / app not verified" screen.)

## 6. Enable Facebook login for your account

The Meta app is in Development mode, so only people with a role on the app can log in.

Ask the project owner to add you as a Tester:

1. Go to https://developers.facebook.com → My Apps → open the Verborum app.
2. Left menu → App Roles → Roles.
3. Click **Add People**, choose Testers, and enter your Facebook name or username → send.

Then you accept the invite:

4. Open https://developers.facebook.com/requests/ (or the notification on Facebook) and accept the Tester request.

After accepting, "Continue with Facebook" works for your account. (Non-test users can't use it until the app passes Meta App Review for the email scope — that's a separate, later step.)

## 7. Point the Android app at this backend

Nothing new to build for social login — the hosted flow already carries it. Just make sure the app targets your local Keycloak and the services:

| Client sees | Value (local dev) |
|---|---|
| Issuer | http://localhost:8180/realms/verborum |
| Client id | verborum-app (public, PKCE S256) |
| Redirect URI | `de.coldtea.verborum://oauth2redirect/<path>` (or `http://localhost:*` on the emulator) |
| Scopes | openid profile email offline_access |
| ms_dictionary | http://10.0.2.2:8085 (emulator) / your LAN IP:8085 (device) |
| ms_user | http://10.0.2.2:8086 (emulator) / your LAN IP:8086 (device) |

Client behaviour that changed and you should handle:

- **Email verification is now required** — after a hosted sign-up, show a "check your email to finish signing up" state (the account can't get tokens until the email is verified).
- **Google/Facebook/email-code all appear inside the same hosted login page** the app already opens — no new screens on your side.

Full client contract: `docs/integration/client-login-guide.md` in the repo.

## 8. Testing on a physical phone (not the emulator)

Keycloak stamps the issuer into every token. On a physical device you hit the backend by LAN IP, so all three must name the same origin or every API call fails with a 401 that looks like a bad token. Set these before `docker compose up` and when starting the services:

```env
KEYCLOAK_HOSTNAME_URL=http://<your-lan-ip>:8180
KEYCLOAK_ISSUER_URI=http://<your-lan-ip>:8180/realms/verborum
KEYCLOAK_JWK_SET_URI=http://<your-lan-ip>:8180/realms/verborum/protocol/openid-connect/certs
```

On the emulator, `localhost` / `10.0.2.2` just work — no changes needed.

## 9. Troubleshooting

| Symptom | Cause / fix |
|---|---|
| Keycloak won't start on first `up` | It's building the custom image — give it a few minutes; check `docker compose logs -f keycloak` |
| Google "access blocked / app not verified" | Your Google account isn't a Test user yet — §5 |
| Facebook login fails / "app not active" | You're not a Tester on the app, or you didn't accept the invite — §6 |
| Social buttons missing | `.env` not filled in, or the stack wasn't restarted after adding it — §3 |
| No verification / code email arrives | Check Mailpit at http://localhost:8025 — mail is captured locally, never actually sent |
| 401 on every API call from a phone | Issuer mismatch — §8 |
| Realm changes not showing | Reset the Keycloak volume — §2.3 |
| `mvnw` can't find Java | Set `JAVA_HOME` to a JDK 17 — §2.2 |

Questions about the auth contract or the client side → `docs/integration/client-login-guide.md`.
Running/verifying the backend → `docs/ops/local-development.md`.
