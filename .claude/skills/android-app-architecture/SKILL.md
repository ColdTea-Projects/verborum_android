---
name: android-app-architecture
description: Apply the structural law of the Verborum Android app — module layout and dependency direction, vertical feature slices, the three model tiers (Entity/Domain/UI) and their converters, layer rules (DAO/Repository/UseCase/Service/ViewModel), Hilt DI, navigation, and the scaffold procedure. Use when deciding where code belongs, adding or restructuring a feature, screen, Room table, or field, or reviewing layering and package placement.
---

# Verborum App Architecture

The **structural law**: how the app is put together and how to extend it. `android-dev` is the day-to-day working companion; this is what it defers to for structure. When adding anything, find the closest existing sibling — `word/` and `dictionary/` in `bibliotheca` are canon — and mirror it rather than inventing a new shape.

## Modules & dependency direction

```
app  →  bibliotheca, forum  →  core          (buildSrc: Configuration.kt for SDK versions)
```

- **`core`** — shared infrastructure: `theme/` (VerborumTheme, VerborumColors), `ui/BaseViewModel`, `di/NetworkModule`, `auth/` (token store, interceptor/authenticator), `extensions/` (shared extension functions, grouped one file per topic as `<Subject>+<Group>.kt` — see the `kotlin` skill). Ships **testFixtures** (`BaseTest`, `MainDispatcherRule`).
- **`bibliotheca`**, **`forum`** — feature library modules. Feature code lives here, never in `app`.
- **`app`** — only `MainActivity`, `VerborumApplication`, and `navigation/` (route constants + nav graph wiring).
- Cross-module references use typesafe accessors: `api(projects.core)`, `testImplementation(testFixtures(projects.core))`. Dependencies point **inward only** — `core` never imports a feature module.

## Feature package layout (a vertical slice)

Each domain concept (`word`, `dictionary`, `auth`, `options`) is a self-contained slice:

```
<feature>/
  data/
    api/            Retrofit interface (XApi) — one interface per BE controller
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
    <screen>/       ONE DIRECTORY PER SCREEN (dictionarylist/, createdictionary/, dictionarydetails/…)
      X.kt          top-level @Composable XScreen(viewModel = hiltViewModel())
      XViewModel.kt @HiltViewModel, extends BaseViewModel
      model/        that screen's state + UI models (XUi with convertToX() back to domain)
      composables/  smaller stateless composables for that screen
    model/          ONLY for UI models several screens of the slice share (word/ui/model/: WordUi, WordMeta)
common/             cross-cutting for the module: data/db (Database, DaoBase), di/, domain/ (SyncService, SyncScheduler…), utils/
```

- **Nothing loose under `ui/`.** No screen file, ViewModel, or `composables/` directly in `ui/` — each belongs to its screen directory. A screen is a directory even when it starts as two files.
- A UI model owned by one screen lives in that screen's `model/`, and other screens import it from there (`DictionaryUi` sits in `dictionarylist/model/`; `CreateDictionaryViewModel` and the domain converters import it). Promote to the slice-level `ui/model/` only once several screens genuinely share it, as `WordUi`/`WordMeta` do.
- `src/test/java/` mirrors this exactly: a test for `ui/dictionarylist/DictionaryListViewModel.kt` goes in `ui/dictionarylist/`, not `ui/`.

## Layer rules

- **Three model tiers**: `XEntity` (Room) ↔ `X` (domain) ↔ `XUi` (Compose). Conversion functions are **methods on the model itself** (`convertToUi()`, `convertToEntity()`, `convertToRequest()`), written field-by-field with named arguments — no mapper classes. A tier never leaks past its layer (no Entity in a ViewModel, no `XUi` in a DAO).
- **DAOs** extend `DaoBase<T>` (generic insert/update/delete with `OnConflictStrategy.REPLACE`); add feature queries with `@Transaction @Query`. Observation queries return `Flow<List<Entity>>`; one-shot reads are `suspend`. REPLACE = DELETE+INSERT, so observation queries **must** carry an explicit `ORDER BY` on a stable column or the list silently reshuffles after a sync.
- **Repositories** are thin: `@Inject constructor(private val db: BibliothecaDatabase)`, expression-body functions delegating to the DAO. No business logic here.
- **Use cases** are single-purpose: one class, one verb, `suspend fun invoke(...)` (or returning `Flow` for observation). Named `<Verb><Noun>[Api]UseCase`, split into `usecase/local/` and `usecase/api/`.
- **Services** (`WordService`, `DictionaryService`, `AuthService`) take use cases + other services via constructor injection and are the **only** class a ViewModel may call. Observation methods map domain→UI: `.map { it.map(X::convertToUi) }.distinctUntilChanged().flowOn(Dispatchers.IO)`.

## ViewModels & UI state

- Extend `core`'s `BaseViewModel` (provides `_snackbarMessages`, `exceptionHandler`, and `Flow<T>.observe(onSuccess, onCompleted, onError)` collecting in `viewModelScope`).
- State: `private val _xState = MutableStateFlow<XState>(XState.Loading)` exposed as `val xState = _xState.asSharedFlow()` (or `asStateFlow()` where a current value is needed).
- State type: a sealed class in `ui/<screen>/model/` with `Loading` / `Success(data)` / `Failed` (`data object` for the empty variants). CRUD failures surface a snackbar via `UiText` and keep the screen; load failures go to `Failed` + `ScreenError`.
- Screens: `@Composable fun XScreen(viewModel: XViewModel = hiltViewModel())`, read state with `collectAsState(initial = Loading)`, branch per state, include a `@Preview` wrapped in `VerborumTheme`.

## DI (Hilt)

- `@InstallIn(SingletonComponent::class) @Module` classes live in `<module>/common/di/` (`DataModule` provides the Room database, `NetworkModule` provides Retrofit/OkHttp/APIs).
- Everything else uses plain `@Inject constructor` — use cases, repositories, services need no module entry.
- ViewModels: `@HiltViewModel class XViewModel @Inject constructor(...)`.

## Navigation (app module)

- Route strings: `const val SCREEN_X = "xScreen"` in `app/.../navigation/Screens.kt`; tab roots are `ScreenGroups` sealed objects registered in `screenGroups`.
- Each screen gets a `NavGraphBuilder` extension in `NavGroupBibliotheca.kt`/`NavGroupForum.kt`/`NavGroupOptions.kt`: `fun NavGraphBuilder.insertX(navController) = composable("$SCREEN_X/{arg}") { … }`, creating the ViewModel with `hiltViewModel()` and calling `viewModel.init(arg)` before the screen. Wire it into `NavigationCentral`.

## Quick start — scaffolding

Find the closest existing sibling, mirror it, then follow the ordered steps in
[references/scaffold_procedure.md](references/scaffold_procedure.md) (new concept, new
screen, or new field — it says which steps apply to which). Verify with:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:testDebugUnitTest :app:assembleDebug
```
