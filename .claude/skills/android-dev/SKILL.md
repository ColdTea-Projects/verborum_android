---
name: android-dev
description: Day-to-day feature development in the Verborum Android app — the working companion for writing data, domain, and UI code across Room/Retrofit/Hilt/Compose. Covers conventions, the edit→verify loop, cross-cutting rules (localization, resources, error handling), and which sibling skill to load for structure, language, UI, tests, security, or build. Load this whenever you write or change production code.
---

# Verborum Android Development

The practical companion for building features. It routes to the specialist skills and captures the conventions that span layers. Mirror the closest existing sibling — `word/` and `dictionary/` in `bibliotheca` are canon.

## Load the right skill

| Doing… | Load |
|---|---|
| Deciding where code goes / adding a concept, screen, table, field | **android-app-architecture** (the law + scaffold procedure) |
| Writing Kotlin — coroutines, Flow, sealed/data classes, immutability | **kotlin** |
| Compose UI, theming, components, accessibility | **material-design** |
| Unit tests (use cases, services, ViewModels) | **android-unit-test** |
| Instrumented / Compose-UI / Room / Hilt tests | **android-integration-test** |
| Tokens, auth, logging, secrets, network security | **android-app-sec** |
| build.gradle.kts, libs.versions.toml, build failures | **gradle-toolchain** |
| Creating/staging/committing files | **git-workflow** |

Load `android-app-architecture` before touching structure; the layering, model tiers, DI, and navigation rules live there and are not repeated here.

## The working loop

1. Read the sibling that most resembles the task before writing.
2. Make the change in the correct layer (data → domain → UI), keeping each tier's model on its own side of the converters.
3. Verify (never claim done without this):
   ```bash
   JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
     ./gradlew :<module>:testDebugUnitTest :app:assembleDebug
   ```
   `:app:assembleDebug` is what runs the AAR-metadata + manifest-merge checks; run it whenever you touch navigation, DI, manifests, or dependencies.

## Cross-cutting rules

- **ViewModels talk only to Services** — never to use cases, repositories, or DAOs. Need new data? Add a method to the Service and use it; don't reach around it.
- **Converters are methods on the model** (`convertToUi()` etc.), field-by-field with named arguments. When you add a field, update every tier + its converters + the `TestFixtures.kt` factories in the same change, or the build breaks in a distant file.
- **Localization is mandatory and synchronized.** Every user-facing string is a resource with a camelCase name, resolved via `stringResource(ResStrings.name)`. It must exist in **all 19 `values-XX/string.xml` locales** (base + 18) with the key present in every file — a missing key in one locale is a latent crash. For a batch of strings, add them to all locales in one pass (a small script keyed on an anchor string is the reliable way). Codes/proper nouns (framework/exam names) stay literal; everything a user reads gets translated.
- **Error handling**: load failures → `Failed` state + `ScreenError` with retry; mutation (CRUD) failures → an error snackbar via `UiText` while the screen stays put. Don't swallow exceptions silently.
- **No-op writes**: Room's REPLACE churns rows and re-emits observers. Skip writes when the new value equals the existing one, and keep observation queries ordered by a stable column.
- **Drawables** are hand-written vector XML in `res/drawable/`, named `ic_<name>_<size>.xml`.

## Boundaries & honesty

- Don't redesign the sync engine (SyncService / SyncScheduler / UploadService / the upload-then-download reconcile) unilaterally — extend the existing try/catch + tombstone + `isSynced` patterns.
- Schema changes need a Room migration and a `version` bump; call out any data-loss risk explicitly.
- Report outcomes faithfully: if a build or test fails, quote the failure; if a step was skipped, say so.

Verify, then finish. When you're done, name the skills you used (see the announcement rule in the root `CLAUDE.md`).
