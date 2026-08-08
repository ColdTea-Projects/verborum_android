---
name: android-unit-test
description: Write and fix JVM unit tests in the Verborum Android app — BaseTest/MainDispatcherRule setup, MockK conventions, test fixtures, ViewModel and Flow state-assertion patterns, and the pitfalls this project has hit. Use when adding or debugging unit tests for use cases, services, repositories, or ViewModels; for instrumented, Compose, Room, or Hilt tests use android-integration-test instead.
---

# Unit Tests (Verborum)

Local JVM tests in `<module>/src/test/java/...` mirroring the main package. Run:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:testDebugUnitTest
```

## Skeleton

```kotlin
class XServiceTest : BaseTest() {           // BaseTest from core testFixtures

    @MockK                                   // io.mockk.impl.annotations.MockK — NEVER io.mockk.MockK
    private lateinit var someUseCase: SomeUseCase

    private lateinit var subject: XService

    override fun setUp() {
        super.setUp()                        // inits @MockK fields (relaxUnitFun = true)
        subject = XService(someUseCase)      // build the subject AFTER super.setUp()
    }

    // region <behavior group>
    @Test
    fun `does the thing when condition`() = runTest { … }
    // endregion
}
```

`BaseTest` provides: `MainDispatcherRule` (Main = `UnconfinedTestDispatcher`, so `viewModelScope` coroutines run **eagerly**), `MockKAnnotations.init(relaxUnitFun = true)`, and `unmockkAll()` in teardown.

## MockK rules (pitfalls we hit)

- Import `io.mockk.impl.annotations.MockK`. `io.mockk.MockK` is an object → "illegal annotation class" compile error.
- `relaxUnitFun` auto-answers **only Unit-returning** functions. Any function returning a value (`saveWord(): Long`, api `Response<Unit>`, expression-body delegates, an inferred non-Unit try/catch) throws `MockKException: no answer found` unless stubbed:
  - value matters → `coEvery { repo.saveWord(any()) } returns 1L`
  - verify-only → declare `@MockK(relaxed = true)`.
- Capture transformed args with `slot<T>()` + `capture(slot)` and assert on `slot.captured`.
- `any()`/`eq()` are matchers used **inside** `every`/`verify` blocks. A call written with defaults (e.g. a 4-arg function stubbed with 3 args) fixes the omitted arg as `eq(default)` — pass an explicit matcher when the real call differs.

## Asserting ViewModel state

State flows are StateFlow-backed (`asSharedFlow()`/`asStateFlow()`) and `viewModelScope` runs eagerly. **Act, then read**:

```kotlin
viewModel.init(dictionaryId)
assertEquals(Expected, viewModel.state.first())
```

**Never** use `launch { state.collect { … } }` + `job.cancel()` inside `runTest` — the collector is scheduled and cancelled before it runs (`NoSuchElementException: List is empty`). Replay-0 `SharedFlow` emissions (snackbars/events) can't be asserted after the fact; assert the observable *state* they cause instead.

## Flows (services / use cases)

- Single emission: `flow.first()` inside `runTest`.
- Multiple / dedup behavior: `flow.toList()` over a finite `flow { emit(...); emit(...) }` stub.
- Error paths: `every { … } returns flow { throw RuntimeException("…") }`.
- Order of interactions: `coVerifyOrder { … }`.

## Fixtures

- Per-module `src/test/.../TestFixtures.kt` with top-level factories (`testWord(...)`, `testDictionaryUi(...)`): every field defaulted, callers override only what matters. **Add a factory for any new model you test.**
- Shared infra (`BaseTest`, `MainDispatcherRule`) lives in `core/src/testFixtures/`, consumed via `testImplementation(testFixtures(projects.core))`.

## Conventions & discipline

- Test names are backtick sentences describing behavior — `` `init emits Failed when dictionary flow throws` ``. Group with `// region … // endregion`.
- One behavior per test; assert on public API (state, Service interactions via `coVerify`), not internals.
- **A failing test that reveals a production bug is a finding, not a license to edit production code silently** — report the failing scenario and let the caller decide.
- Adding a constructor param to a Service/use case breaks its test's `setUp()` — fix the test constructor + add a mock in the same change; keep the suite green.

## Scope

This skill is JVM unit tests only. Compose-UI / instrumented / Room-DAO / Hilt-integration tests are a different setup — see **android-integration-test**.
