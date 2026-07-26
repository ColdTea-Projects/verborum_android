# Verborum Android

Personal vocabulary platform: users build dictionaries of word pairs between two languages, enrich entries with grammatical detail, and practice them. Local-first (Room is the working copy, backend the durable store); native Kotlin + Jetpack Compose, permanently — the planned Web/iOS clients are separate KMP repos.

## Layout

```
app  →  bibliotheca, forum  →  core        (buildSrc: Configuration.kt for SDK versions)
```

Domain feature code lives in `bibliotheca`/`forum` (vertical slices: `data/` → `domain/` → `ui/`), never in `app` (only MainActivity, Application, navigation). `core` holds theme, BaseViewModel, network DI, testFixtures, plus the two app-wide slices — `auth/` (token layer, AppAuth, login screen) and `options/`. Since `core` cannot depend on a feature module, feature work triggered by login is contributed through the `PostLoginHook` multibinding. Versions only in `gradle/libs.versions.toml`; KSP, never kapt.

Build/test (JAVA_HOME required):

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:assembleDebug
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:testDebugUnitTest
```

## Skills — load BEFORE touching the matching area

The skills in `.claude/skills/` are the project's rulebook. Read the matching SKILL.md before writing code, not after something breaks. `android-dev` is the entry point for any production code; it routes to the rest.

| Skill | Load when |
|---|---|
| `android-dev` | Any production-code task — the working companion (conventions, edit→verify loop, localization, error handling) and router to the others. Start here. |
| `android-app-architecture` | Deciding where code goes; adding a concept, screen, table, or field — the structural law (layering, model tiers, DI, navigation) + the scaffold procedure. |
| `kotlin` | Writing/reviewing Kotlin — coroutines/Flow, dispatchers, immutability, null-safety, serialization idioms. |
| `material-design` | Any Compose UI — theming via `colorScheme`, shared components, accessibility, light/dark + RTL. |
| `android-unit-test` | JVM unit tests (use cases, services, ViewModels) — BaseTest/MockK conventions and pitfalls. |
| `android-integration-test` | Room/migration, Compose-UI, Hilt, or MockWebServer tests (note: not set up yet — this covers standing it up). |
| `android-app-sec` | Auth, tokens, the network layer, logging/secrets, network-security config, or release hardening. |
| `gradle-toolchain` | Touching build.gradle.kts, libs.versions.toml, gradle.properties, or diagnosing build failures. |
| `git-workflow` | Creating files, staging, or committing — this repo auto-stages Write-created files via a hook. |

A single task usually spans several skills — e.g. a new screen pulls `android-dev` + `android-app-architecture` + `kotlin` + `material-design` + `android-unit-test`. Load all that apply.

## Agents

Specialist subagents in `.claude/agents/` (each already knows which skills to read). Use them when the user asks for delegation; otherwise work inline with the skills above.

- **`android-dev`** — full-stack feature developer (data + domain + UI + unit tests, end to end). The former android-data/android-ui/android-test agents are merged into this one.
- **`android-code-review`** — reviews a diff/branch/PR against the architecture, Kotlin, Material, security, and testing standards; read-and-report only, returns ranked findings.
- **`android-build`** — Gradle/version-catalog/toolchain and build-failure diagnosis (reads `gradle-toolchain`).

## Announce skills used

At the end of any response where you applied one or more skills, add a one-line hint naming them, e.g. `(skills used: android-dev, gradle-toolchain)`. Keep it to the skills actually followed for that task.

## Docs — product & platform context

`docs/` holds the design documents (read the .md files; ignore any PDFs):

- `docs/android-development.md` — project goals, feature status, architecture, the **canonical word-storage contract** (surfaces/meta JSON), sync engine design, backend endpoints, and the roadmap. Read before product/feature-scope decisions or anything touching word storage or sync.
- `docs/frontend-backend-integration.md` — cross-client platform picture, API and auth contracts, environments, compatibility rules. Read before touching Retrofit APIs, DTOs, auth, or sync behavior. Canonical copy lives in the backend repo; this is a reference copy.
- `docs/client-login-guide.md` — practical companion to the integration §6: how to build the client login against Keycloak (OAuth PKCE via AppAuth, hosted sign-up, token handling, logout, guest-data migration). Read before touching auth, login/logout, or token/refresh handling.
- `docs/production-cutover.md` — checklist for moving the client off the local dev stack (localhost/http) to real HTTPS servers: base URLs, cleartext/network-security, release hardening, and other go-live steps. Read when preparing a release or changing environment config.

## Hard rules

- Release policy: nothing ships until Android + backend are ready as a pair; current guest/offline mode is a development state.
- `BibliothecaDatabase` schema changes need a `version` bump **and** a matching `Migration` in `addMigrations(...)` — a missed migration is a data-loss/crash risk; flag them.
- Never commit or push unless explicitly asked. `local.properties`, `build/`, keystores stay out of git.
- UI strings go through resources and must be kept in sync across all 19 `values-XX/` locales.
