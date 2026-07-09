---
name: write-tests
description: Write or fix unit tests in the Verborum Android project — BaseTest/MainDispatcherRule setup, MockK conventions, test fixtures, ViewModel state assertion patterns, and known pitfalls. Use when adding tests for use cases, services, repositories, or ViewModels, or when diagnosing failing/flaky unit tests.
---

# Write Unit Tests (Verborum)

Tests live in `<module>/src/test/java/...` mirroring the main package. Run with:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:testDebugUnitTest
```

## Test class skeleton

```kotlin
class XServiceTest : BaseTest() {          // BaseTest from core testFixtures

    @MockK                                  // io.mockk.impl.annotations.MockK — NEVER io.mockk.MockK
    private lateinit var someUseCase: SomeUseCase

    private lateinit var subject: XService

    override fun setUp() {
        super.setUp()                       // initializes @MockK fields (relaxUnitFun = true)
        subject = XService(someUseCase)
    }

    // region <behavior group>
    @Test
    fun `does the thing when condition`() = runTest { … }
    // endregion
}
```

`BaseTest` provides: `MainDispatcherRule` (Main = `UnconfinedTestDispatcher`, so `viewModelScope` coroutines run **eagerly**), MockK annotation init with `relaxUnitFun = true`, and `unmockkAll()` in teardown.

## MockK rules (pitfalls we actually hit)

- Import `io.mockk.impl.annotations.MockK`. `io.mockk.MockK` is an object → "illegal annotation class" compile error.
- `relaxUnitFun` auto-answers **only Unit-returning** functions. Any suspend function returning a value (`saveWord(): Long`, api `Response<Unit>`, expression-body delegates) throws `MockKException: no answer found` unless stubbed:
  - Stub when the value matters: `coEvery { repo.saveWord(any()) } returns 1L`
  - Or declare `@MockK(relaxed = true)` when the mock is verify-only.
- Capture args with `slot<T>()` + `capture(slot)` to assert on transformed values.
- `any()` is a matcher used *inside* `every`/`verify` blocks — it is not importable as `io.mockk.any`.

## Asserting ViewModel state

State flows are StateFlow-backed (`asSharedFlow()` with replay) and `viewModelScope` runs eagerly under the rule. So: **act, then read**:

```kotlin
viewModel.init(dictionaryId)
assertEquals(Expected, viewModel.state.first())
```

**Never** use `launch { state.collect { list.add(it) } }` + `job.cancel()` inside `runTest` — the collector runs on the test scheduler and is cancelled before it executes (`NoSuchElementException: List is empty`).

## Testing flows (services/use cases)

- Single emission: `flow.first()` inside `runTest`.
- Multiple/dedup behavior: `flow.toList()` on a finite `flow { emit(...); emit(...) }` stub.
- Error paths: `every { … } returns flow { throw RuntimeException("…") }`.

## Fixtures

- Per-module `src/test/.../TestFixtures.kt` with top-level factories (`testWord(...)`, `testWordUi(...)`): every field has a default, callers override only what the test cares about. Add factories for any new model you test.
- Shared infra (`BaseTest`, `MainDispatcherRule`) lives in `core/src/testFixtures/` — consumed via `testImplementation(testFixtures(projects.core))`.

## Conventions

- Test names: backtick sentences describing behavior — `` `init emits Failed when dictionary flow throws` ``.
- Group related tests with `// region name` / `// endregion`.
- One behavior per test; assert on state/interactions, not implementation details.

## Scope limits

Instrumented/Compose UI testing is **not set up** in this project (only generated `ExampleInstrumentedTest` stubs exist). If UI tests are requested, that's new infrastructure — confirm with the user before adding it.
