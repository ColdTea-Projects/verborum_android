---
name: gradle-toolchain
description: Manage Gradle configuration, dependencies, and the build toolchain for the Verborum Android project — version-catalog rules, Kotlin/KSP/Hilt version alignment, adding dependencies safely, build variants, and diagnosing build failures including swallowed annotation-processor errors. Use when touching build.gradle.kts, libs.versions.toml, gradle.properties, or buildSrc, and when a build or Gradle task fails.
---

# Gradle & Toolchain (Verborum)

## Where things live

- **All versions**: `gradle/libs.versions.toml` (catalog). Never hardcode a version in a `build.gradle.kts`.
- **SDK levels**: `buildSrc/.../Configuration.kt` — `minSdk 23`, `targetSdk 35`, `compileSdk 35`. AGP is 8.7.0.
- **Global flags**: `gradle.properties` — includes `android.experimental.enableTestFixturesKotlinSupport=true` (required for core's Kotlin testFixtures; do not remove).
- Modules: `app` (application), `bibliotheca`/`forum`/`core` (libraries). Cross-module deps use `projects.` accessors.

## Invariants — check before ANY version change

| Rule | Current |
|---|---|
| Kotlin android plugin uses `version.ref = "kotlin"` (never pinned separately) | kotlin = 2.1.0 |
| KSP version = `<kotlin>-<ksp>` matching the Kotlin version | 2.1.0-1.0.29 |
| ALL Hilt artifacts (android, compiler, plugin, testing) share one version | 2.52 |
| kotlinx-serialization plugin version matches Kotlin | 2.1.0 |
| **KSP, never kapt** for Hilt + Room | `ksp(libs.hilt.compiler)` etc. |

Why no kapt: it's legacy on Kotlin 2.x (falls back to language level 1.9) and its stub layer caused opaque, KGP-swallowed crashes here (including "Provided Metadata instance has version X" errors). If someone adds a `kapt(...)` line, convert it to `ksp(...)`.

## Adding a dependency

```toml
# gradle/libs.versions.toml
[versions]
turbine = "1.2.0"

[libraries]
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
```

```kotlin
// <module>/build.gradle.kts
testImplementation(libs.turbine)
```

1. Add the version under `[versions]` and the alias under `[libraries]` in the catalog. Watch the naming quirk: catalog uses both `snake_case` and `kebab-case` aliases; follow neighboring entries.
2. **Check it doesn't already exist under another alias** — a duplicate artifact at two versions broke `checkDebugAarMetadata` before (core-ktx declared twice).
3. **Check AAR metadata compatibility**: new androidx versions may require a newer compileSdk/AGP than 35/8.7.0. If `:app:checkDebugAarMetadata` fails after adding, prefer an older compatible version of the dependency over bumping AGP.
4. Compose artifacts: versionless via the BOM (`platform(libs.androidx.activity.compose.bom)`).
5. Test-only deps: `testImplementation`; shared test infra via `testImplementation(testFixtures(projects.core))`.

## Diagnosing build failures

- No `java` on PATH: prefix commands with
  `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- **KSP/processor errors are sometimes swallowed** ("A failure occurred while executing … WorkAction" with no cause). Escalation ladder:
  1. `--console=plain` and read the *full* output, not just the tail.
  2. `./gradlew --stop`, delete the module's `build/`, rerun with `--rerun-tasks`.
  3. Rerun the failing task with `--info`, grep for `e: ` and `Caused by`.
  4. Check the daemon log: `~/.gradle/daemon/<version>/daemon-*.out.log`.
  5. If Hilt/metadata related: verify the invariants table above first — version skew is the usual culprit.
- After catalog changes always verify both: `:<module>:testDebugUnitTest` **and** `:app:assembleDebug` (the AAR metadata check only runs for the app).

## Build variants & release (current state)

- `app`: release is minified + shrunk with default ProGuard; debug is not minified. Library modules ship `consumer-rules.pro` (currently empty conventions).
- **No CI/CD, signing config, or Play Store pipeline exists in this repo.** Don't invent one silently — if release automation is requested, propose it as new infrastructure first.
