---
name: scaffold-feature
description: Scaffold a new feature slice or screen in the Verborum Android app — entity, DAO, repository, use cases, service, ViewModel, Compose screen, navigation wiring, and string resources. Use when adding a new domain concept, screen, database table, or API endpoint. Covers Room, Retrofit, Hilt, Compose, and navigation steps in the order this project expects.
---

# Scaffold a Feature (Verborum)

Prerequisite: read `.claude/skills/android-dev/SKILL.md` for the architecture rules. This skill is the *procedure*; that one is the *law*. Copy patterns from the closest existing sibling — `word/` and `dictionary/` in `bibliotheca` are the canon.

First decide the scope, then execute only the needed steps in order:

- **New domain concept** (new table/API resource): all steps.
- **New screen over existing data**: steps 4–6 only.
- **New field on existing concept**: touch every model tier + converters + fixtures (step 7), bump DB version if the entity changed.

## 1. Data layer — local (Room)

1. `data/db/entity/XEntity.kt` — `@Entity(tableName = "x", primaryKeys = ["x_id"])`, snake_case `@ColumnInfo` names, defaults for `isSynced = false`, `createdAt = getNowInMillis()`.
2. `data/db/dao/DaoX.kt` — `@Dao interface DaoX : DaoBase<XEntity>` with `@Transaction @Query` methods. `Flow<List<XEntity>>` for observation, `suspend` for one-shots.
3. Register in `common/data/db/BibliothecaDatabase.kt`: add the entity to `@Database(entities = [...])`, add the `daoX` accessor, and **bump `version`** if the schema changed (no migrations are configured — flag this to the user if data loss matters).
4. `data/XRepository.kt` — `@Inject constructor(private val bibliothecaDatabase: BibliothecaDatabase)`, thin expression-body delegates to the DAO.

## 2. Data layer — remote (Retrofit), only if the concept syncs

1. `data/api/model/XRequest.kt` / `XResponse.kt` — `@Keep @Serializable` with explicit `@SerialName` on every field.
2. `data/api/XApi.kt` — Retrofit interface, `suspend` functions returning `Response<T>`.

## 3. Domain layer

1. `domain/model/X.kt` — plain data class with `convertToEntity()`, `convertToUi()`, and (if syncing) `convertToRequest()` as methods, field-by-field with named arguments.
2. `domain/usecase/local/` and `usecase/api/` — one class per verb: `SaveXUseCase`, `ObserveXByYUseCase`, `DeleteXUseCase`… Each is `class … @Inject constructor(repo/api) { suspend fun invoke(...) = … }`.
3. `domain/XService.kt` — orchestrates use cases; the only class ViewModels may call. Observation methods map domain→UI: `.map { it.map(X::convertToUi) }.distinctUntilChanged().flowOn(Dispatchers.IO)`.

## 4. UI layer

1. `ui/<screen>/model/XScreenState.kt` — sealed class: `data object Loading`, `data class Success(...)`, `data object Failed`.
2. `ui/<screen>/XViewModel.kt` — `@HiltViewModel`, extends `BaseViewModel`, `MutableStateFlow(Loading)` exposed via `asSharedFlow()`, an `init(id: String)` fn using `Flow.observe(onSuccess, onError)` from `BaseViewModel`.
3. `ui/<screen>/X.kt` — `@Composable fun XScreen(viewModel: XViewModel = hiltViewModel())`, state via `collectAsState(initial = Loading)`, branch per state. Add a `@Preview` wrapped in `VerborumTheme`.
4. `ui/<screen>/composables/` — extract stateless child composables here; hoist state and pass lambdas down.

## 5. Navigation (app module)

1. Add `const val SCREEN_X = "xScreen"` in `app/.../navigation/Screens.kt`.
2. Add `fun NavGraphBuilder.insertX(navController: NavHostController) = composable("$SCREEN_X/{arg}") { … }` in the module's NavGroup file (`NavGroupBibliotheca.kt` / `NavGroupForum.kt`) — create the ViewModel with `hiltViewModel()`, read args from `navBackStackEntry.arguments`, call `viewModel.init(arg)` before the screen.
3. Wire the new `insertX(...)` call into `NavigationCentral.kt`.

## 6. Resources

- Strings: module-local `res/values/string.xml` (note: singular filename in bibliotheca), **camelCase names** (`dictionaryListScreenHeader`). Access via `stringResource(ResStrings.name)` — `ResStrings` is the module's `R.string` typealias in `common/utils/Resources.kt`.
- Drawables: hand-written vector XMLs in `res/drawable/`, named `ic_<name>_<size>.xml`.
- DI: normally nothing to add — constructor `@Inject` covers use cases/repos/services. Only touch `common/di/` when providing something external (database, Retrofit).

## 7. Tests & fixtures

- Add factory functions for every new model tier to `src/test/.../TestFixtures.kt` (`testXEntity`, `testX`, `testXUi`) — all fields defaulted.
- Write unit tests per `.claude/skills/write-tests/SKILL.md` for the use cases, service, and ViewModel.

## 8. Verify

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:testDebugUnitTest :app:assembleDebug
```

Both must pass before the feature is done.
