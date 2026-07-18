# Verborum Android

Personal vocabulary platform: users build dictionaries of word pairs between two languages, enrich entries with grammatical detail, and practice them. Local-first (Room is the working copy, backend the durable store); native Kotlin + Jetpack Compose, permanently — the planned Web/iOS clients are separate KMP repos.

## Layout

```
app  →  bibliotheca, forum  →  core        (buildSrc: Configuration.kt for SDK versions)
```

Feature code lives in `bibliotheca`/`forum` (vertical slices: `data/` → `domain/` → `ui/`), never in `app` (only MainActivity, Application, navigation). `core` holds theme, BaseViewModel, network DI, testFixtures. Versions only in `gradle/libs.versions.toml`; KSP, never kapt.

Build/test (JAVA_HOME required):

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:assembleDebug
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:testDebugUnitTest
```

## Skills — load BEFORE touching the matching area

The skills in `.claude/skills/` are the project's rulebook. Read the matching SKILL.md before writing code, not after something breaks:

| Skill | Load when |
|---|---|
| `android-dev` | Any production code — the architecture law (layering, models, naming). Prerequisite for the others. |
| `scaffold-feature` | Adding a domain concept, screen, table, or endpoint — the step-by-step procedure. |
| `write-tests` | Writing or fixing unit tests — BaseTest/MockK conventions and known pitfalls. |
| `gradle-toolchain` | Touching build.gradle.kts, libs.versions.toml, gradle.properties, or diagnosing build failures. |
| `git-workflow` | Creating files, staging, or committing — this repo auto-stages Write-created files via a hook. |

## Agents

Specialist subagents in `.claude/agents/` (each already knows which skills to read): `android-data` (Room/repos/use cases/DI), `android-ui` (Compose/ViewModel state/navigation), `android-test` (unit tests), `android-build` (Gradle/toolchain). Use them when the user asks for delegation; otherwise work inline with the skills above.

## Docs — product & platform context

`docs/` holds the design documents (read the .md files; ignore any PDFs):

- `docs/android-development.md` — project goals, feature status, architecture, the **canonical word-storage contract** (surfaces/meta JSON), sync engine design, backend endpoints, and the roadmap. Read before product/feature-scope decisions or anything touching word storage or sync.
- `docs/frontend-backend-integration.md` — cross-client platform picture, API and auth contracts, environments, compatibility rules. Read before touching Retrofit APIs, DTOs, auth, or sync behavior. Canonical copy lives in the backend repo; this is a reference copy.

## Hard rules

- Release policy: nothing ships until Android + backend are ready as a pair; current guest/offline mode is a development state.
- `BibliothecaDatabase` has no migrations — schema changes need a version bump and are data-loss risks; flag them.
- Never commit or push unless explicitly asked. `local.properties`, `build/`, keystores stay out of git.
- UI strings go through resources and must be kept in sync across all 10 `values-XX/` locales.
