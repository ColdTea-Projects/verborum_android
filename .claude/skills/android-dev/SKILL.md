---
name: android-dev
description: Android development in the Verborum codebase — module layout, clean-architecture layering (entity/domain/UI models, use cases, services), Compose + Hilt + Room + Retrofit conventions, build toolchain (KSP, version catalog), and unit-testing patterns. Use when adding features, screens, ViewModels, DAOs, use cases, tests, or touching Gradle config in this project.
---

# Verborum Android Development

Follow the existing architecture exactly. When adding anything new, find the closest existing sibling (e.g. `word/` vs `dictionary/`) and mirror it.

## Modules & dependency direction

```
app  →  bibliotheca, forum  →  core          (buildSrc: Configuration.kt for SDK versions)
```

- **`core`** — shared infrastructure: `theme/` (VerborumTheme, VerborumColors), `ui/BaseViewModel`, `di/NetworkModule`, `extensions/`. Also ships **testFixtures** (`BaseTest`, `MainDispatcherRule`).
- **`bibliotheca`**, **`forum`** — feature library modules. Feature code lives here, never in `app`.
- **`app`** — only `MainActivity`, `VerborumApplication`, and `navigation/` (route constants + nav graph wiring).
- Modules reference each other via typesafe accessors: `api(projects.core)`, `testImplementation(testFixtures(projects.core))`.

## Feature package layout (inside a feature module)

Each domain concept (e.g. `word`, `dictionary`) is a vertical slice:

```
<feature>/
  data/
    api/            Retrofit interface (XApi)
    api/model/      @Serializable request/response DTOs
    db/dao/         Room DAO (DaoX : DaoBase<Entity>)
    db/entity/      Room @Entity (XEntity)
    XRepository.kt  wraps the database/DAO, @Inject constructor
  domain/
    model/          domain model (X) with convertToEntity()/convertToUi()/convertToRequest()
    usecase/local/  one-verb use cases hitting the repository (SaveXUseCase, ObserveXByYUseCase…)
    usecase/api/    one-verb use cases hitting the API (SaveXApiUseCase…)
    XService.kt     orchestrates use cases; the only thing ViewModels talk to
  ui/
    X.kt            top-level @Composable XScreen(viewModel = hiltViewModel())
    XViewModel.kt   @HiltViewModel, extends BaseViewModel
    model/          UI model (XUi) with convertToX() back to domain
    composables/    smaller stateless composables for that screen
common/             cross-cutting for the module: data/db (Database, DaoBase), di/, domain/ (SyncService…), utils/
```

## Layer rules

- **Three model tiers**: `XEntity` (Room) ↔ `X` (domain) ↔ `XUi` (Compose). Conversion functions are *methods on the model itself* (`convertToUi()`, `convertToEntity()`, `convertToWord()`), written field-by-field with named arguments — no mapper classes.
- **DAOs** extend `DaoBase<T>` (generic insert/update/delete with `OnConflictStrategy.REPLACE`); add feature queries with `@Transaction @Query`. Observation queries return `Flow<List<Entity>>`, one-shot reads are `suspend`.
- **Repositories** are thin: `@Inject constructor(private val db: BibliothecaDatabase)`, expression-body functions delegating to the DAO.
- **Use cases** are single-purpose classes with `suspend fun invoke(...)` (or returning `Flow` for observation), named `<Verb><Noun>[Api]UseCase`, split into `local/` and `api/`.
- **Services** (`WordService`, `DictionaryService`) take use cases + other services via constructor injection, map domain→UI models (`.map { it.map(X::convertToUi) }.distinctUntilChanged().flowOn(Dispatchers.IO)`), and are the ViewModel-facing API.

## ViewModels & UI state

- Extend `core`'s `BaseViewModel` (provides `_snackbarMessages`, `exceptionHandler`, and `Flow<T>.observe(onSuccess, onCompleted, onError)` which collects in `viewModelScope`).
- State: `private val _xState = MutableStateFlow<XState>(XState.Loading)` exposed as `val xState = _xState.asSharedFlow()`.
- State type: sealed class in `ui/<screen>/model/` with `Loading` / `Success(data)` / `Failed` variants (`data object` for the empty ones).
- Screens take `viewModel: XViewModel = hiltViewModel()`, read state with `collectAsState(initial = XState.Loading)`, and render per state branch. Include a `@Preview` composable wrapped in `VerborumTheme`.

## Navigation (app module)

- Route strings are `const val SCREEN_X = "xScreen"` in `app/.../navigation/Screens.kt`; groups are `ScreenGroups` sealed objects.
- Each screen gets a `NavGraphBuilder` extension in `NavGroupBibliotheca.kt` / `NavGroupForum.kt`: `fun NavGraphBuilder.insertX(navController) = composable("$SCREEN_X/{arg}") { … }`, creating the ViewModel with `hiltViewModel()` and calling `viewModel.init(arg)` before the screen composable. Wire it in `NavigationCentral`.

## DI (Hilt)

- Modules: `@InstallIn(SingletonComponent::class) @Module` classes in `<module>/common/di/` (`DataModule` provides the Room database, `NetworkModule` provides Retrofit/OkHttp).
- Everything else uses plain `@Inject constructor` — use cases, repositories, services need no module entries.
- ViewModels: `@HiltViewModel class XViewModel @Inject constructor(...)`.

## Build toolchain (do not regress these)

- **KSP, never kapt** — `alias(libs.plugins.ksp)` + `ksp(libs.hilt.compiler)` / `ksp(libs.room.compiler)`. kapt is legacy on Kotlin 2.x and previously caused opaque, swallowed build crashes here.
- All versions live in `gradle/libs.versions.toml`. Keep aligned: `kotlin` and the Kotlin android plugin share one `version.ref`; `ksp` must match the Kotlin version (`2.1.0` ↔ `2.1.0-1.0.29`); **all Hilt artifacts pinned to the same version** (currently 2.52).
- API DTOs use kotlinx.serialization: `@Keep @Serializable` with explicit `@SerialName` on every field; Retrofit converter is `retrofit2-kotlinx-serialization-converter`.
- `gradle.properties` has `android.experimental.enableTestFixturesKotlinSupport=true` — required for core's Kotlin testFixtures to compile. Core's testFixtures also need the Compose runtime on their classpath (module-wide Compose compiler).
- SDK versions come from `buildSrc` `Configuration` (compileSdk 35, AGP 8.7) — check AAR-metadata compatibility before bumping any androidx dependency.
- Never add the same artifact twice under two catalog aliases (this happened with core-ktx and broke `checkDebugAarMetadata`).

## Unit testing

- Test classes extend `core`'s **`BaseTest`** (testFixtures): it runs `MockKAnnotations.init(this, relaxUnitFun = true)`, applies `MainDispatcherRule` (`UnconfinedTestDispatcher` — Main-dispatcher coroutines run eagerly), and `unmockkAll()` after each test. Override `setUp()`, call `super.setUp()` first, then build the subject under test.
- Mock annotation import is **`io.mockk.impl.annotations.MockK`** (NOT `io.mockk.MockK` — that's an object and produces "illegal annotation class").
- `relaxUnitFun` covers only `Unit`-returning functions. For suspend functions returning values (`saveWord(): Long`, api calls returning `Response<Unit>`): stub with `coEvery { … } returns …`, or declare `@MockK(relaxed = true)` for verify-only mocks.
- Test names: backtick sentences (`` fun `init emits Failed when dictionary flow throws`() ``), grouped with `// region … // endregion`.
- Fixture data: top-level factory functions in `src/test/.../TestFixtures.kt` (`testWord(...)`, `testWordUi(...)`) with every field defaulted and overridable.
- Asserting ViewModel state: the state flows are StateFlow-backed with replay, and `viewModelScope` work runs eagerly under the rule — so act, then assert with `viewModel.xState.first()`. Do **not** use the `launch { collect }` + `job.cancel()` pattern inside `runTest`; the collector never runs.
- Flow assertions in services: `.first()` / `.toList()` inside `runTest`; MockK `every { … } returns flowOf(...)` or `flow { … }` for error cases.
- Run with `./gradlew :<module>:testDebugUnitTest`. If `java` isn't on PATH, use `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
