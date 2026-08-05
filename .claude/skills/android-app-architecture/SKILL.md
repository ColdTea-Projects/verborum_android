---
name: android-app-architecture
description: The architecture of the Verborum Android app — module layout and dependency direction, the vertical feature-slice structure, the three model tiers (Entity/Domain/UI) and their converters, layer rules (DAO/Repository/UseCase/Service/ViewModel), Hilt DI, navigation, and the step-by-step procedure for scaffolding a new feature, screen, table, or field. Load this before adding or restructuring any production code, or when deciding where something belongs.
---

# Verborum App Architecture

This skill is the **law and the procedure**: it defines how the app is structured and how to extend it. `android-dev` is the day-to-day working companion; this is what it defers to for structure. When adding anything, find the closest existing sibling — `word/` and `dictionary/` in `bibliotheca` are canon — and mirror it rather than inventing a new shape.

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
    X.kt            top-level @Composable XScreen(viewModel = hiltViewModel())
    XViewModel.kt   @HiltViewModel, extends BaseViewModel
    model/          UI model (XUi) with convertToX() back to domain
    composables/    smaller stateless composables for that screen
common/             cross-cutting for the module: data/db (Database, DaoBase), di/, domain/ (SyncService, SyncScheduler…), utils/
```

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

---

# Procedure: scaffold a feature

Decide scope first, then run only the needed steps in order:

- **New domain concept** (new table/API resource): all steps.
- **New screen over existing data**: steps 4–6.
- **New field on an existing concept**: touch every model tier + converters + fixtures (step 7); add a Room migration + bump `version` if the entity changed.

### 1. Data — local (Room)
1. `data/db/entity/XEntity.kt` — `@Entity(tableName = "x", primaryKeys = ["x_id"])`, snake_case `@ColumnInfo`, defaults (`isSynced = false`, `createdAt = getNowInMillis()`).
2. `data/db/dao/DaoX.kt` — `@Dao interface DaoX : DaoBase<XEntity>`; `Flow<List<XEntity>>` for observation (with explicit `ORDER BY`), `suspend` for one-shots.
3. Register in `common/data/db/BibliothecaDatabase.kt`: add to `@Database(entities=[...])`, add the `daoX` accessor, **bump `version`** and add a `Migration(n, n+1)` to `addMigrations(...)` when the schema changes (there are real migrations here — never drop data silently; flag the risk).
4. `data/XRepository.kt` — thin `@Inject constructor(db)` delegates.

### 2. Data — remote (Retrofit), only if it syncs
1. `data/api/model/XRequest.kt`/`XResponse.kt` — `@Keep @Serializable`, explicit `@SerialName` per field, nullable + defaulted for fields the backend may omit.
2. `data/api/XApi.kt` — one Retrofit interface **per backend controller** (a distinct controller = a distinct interface, even on the same origin), `suspend` returning `Response<T>` (or a nullable body for GET lists).

### 3. Domain
1. `domain/model/X.kt` — data class with `convertToEntity()`/`convertToUi()`/`convertToRequest()`.
2. `domain/usecase/local/` + `usecase/api/` — one verb per class.
3. `domain/XService.kt` — orchestrates; the ViewModel-facing API.

### 4. UI
1. `ui/<screen>/model/XScreenState.kt` — sealed `Loading`/`Success`/`Failed`.
2. `ui/<screen>/XViewModel.kt` — `@HiltViewModel : BaseViewModel`, `MutableStateFlow(Loading)`, `init(id)` using `Flow.observe(...)`.
3. `ui/<screen>/X.kt` — the screen + a `@Preview` in `VerborumTheme` (see the `material-design` skill for component/theming rules).
4. `ui/<screen>/composables/` — stateless children; hoist state, pass lambdas down.

### 5. Navigation (app module)
Add `SCREEN_X`, an `insertX(...)` extension, and wire it into `NavigationCentral`.

### 6. Resources
- Strings: module `res/values/string.xml` (singular filename in bibliotheca), **camelCase** names, via `stringResource(ResStrings.name)`. **Every user-facing string must be added to all 19 `values-XX/` locales in sync** (see `android-dev`).
- Drawables: hand-written vector XML in `res/drawable/`, `ic_<name>_<size>.xml`.

### 7. Tests & fixtures
Add `testX*` factories to `TestFixtures.kt`; write unit tests per the `android-unit-test` skill.

### 8. Verify
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:testDebugUnitTest :app:assembleDebug
```
Both must pass before the feature is done.
