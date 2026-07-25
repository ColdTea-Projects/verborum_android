---
name: android-integration-test
description: Integration and instrumented testing for the Verborum Android app — Room DAO/migration tests, Compose UI tests, Hilt test harness, and Retrofit-against-MockWebServer. Load when asked to test real component wiring (DB queries, migrations, screen behavior, DI graph) rather than isolated logic. Note the project has no instrumented setup yet; this skill also covers standing it up.
---

# Integration & Instrumented Tests (Verborum)

Use this for tests that exercise **real component wiring** — Room queries against a real (in-memory) database, schema migrations, Compose screen behavior, the Hilt graph, or Retrofit against a fake server. For isolated logic (use cases, services, ViewModels with mocked deps), use **android-unit-test**.

## Current state — read first

The project has **no working integration/instrumented tests**: only generated `ExampleInstrumentedTest` / `ExampleUnitTest` stubs exist, no `HiltTestRunner`, no Compose test rule wired, no MockWebServer. So any request here is **new infrastructure** — confirm scope with the user before adding dependencies, and add it in one coherent, verified pass. Prefer the cheapest tier that actually covers the risk (a Robolectric or in-memory Room test often beats a full emulator run).

## Room DAO & migration tests (highest value, cheapest)

Room DAO logic (the `@Query`s, `ORDER BY`, tombstone filters, `reassignOwner`, tag JSON) is real integration risk that unit tests can't cover.

- Build an **in-memory** database: `Room.inMemoryDatabaseBuilder(context, BibliothecaDatabase::class.java).allowMainThreadQueries().build()`; get `context` from `ApplicationProvider` (Robolectric under `testDebugUnitTest`, or `androidTest`).
- **Migrations must be tested** — this DB has real migrations (`MIGRATION_1_2`, `MIGRATION_2_3`) and version bumps are data-loss risks. Use `androidx.room.testing.MigrationTestHelper`: create at the old version with seed rows, run the migration, assert columns/data survived. Every new migration gets a test.
- Assert Flow-returning queries with Turbine or by collecting the first emission on a test dispatcher.

## Compose UI tests

- `createAndroidComposeRule<…>()` (or `createComposeRule()` for a component in isolation), then `onNodeWithText`/`onNodeWithContentDescription` → `assertIsDisplayed()` / `performClick()`.
- Prefer testing a **stateless content composable** (e.g. `LoginContent`, `OptionsContent`) with hoisted state and lambda callbacks — the screens are already split this way, so you avoid needing a real ViewModel/Hilt.
- Every interactive element needs a stable `contentDescription`/text to be findable — this doubles as accessibility (see **material-design**).

## Hilt integration tests

Only when verifying the DI graph or a real ViewModel end to end. Needs `@HiltAndroidTest`, a custom `HiltTestRunner` (`AndroidJUnitRunner` creating `HiltTestApplication`) wired in `defaultConfig.testInstrumentationRunner`, `@HiltAndroidRule`, and `@TestInstallIn` modules to swap network/db for fakes. This is the heaviest tier — justify it.

## Retrofit / network

Drive the API layer against **MockWebServer** (`okhttp3.mockwebserver`): enqueue canned JSON, point a test Retrofit at `server.url("/")`, and assert the DTO parsing + `Response` handling. Good for auth flows (401→refresh→retry via `TokenAuthenticator`) and the tag sub-resource reconcile without a live backend.

## Guardrails

- Keep tests **hermetic**: no real network, no `localhost` backend, no device state — fakes/in-memory only.
- Don't weaken production visibility (making things `public`/`open`) just to test — test through the public surface or a stateless composable.
- Announce this as new infra and report the exact dependencies/runner you added and the verification command used.
