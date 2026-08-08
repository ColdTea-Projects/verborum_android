# Procedure: scaffold a feature

Decide scope first, then run only the needed steps in order:

- **New domain concept** (new table/API resource): all steps.
- **New screen over existing data**: steps 4–6.
- **New field on an existing concept**: touch every model tier + converters + fixtures (step 7); add a Room migration + bump `version` if the entity changed.

## 1. Data — local (Room)

1. `data/db/entity/XEntity.kt` — `@Entity(tableName = "x", primaryKeys = ["x_id"])`, snake_case `@ColumnInfo`, defaults (`isSynced = false`, `createdAt = getNowInMillis()`).
2. `data/db/dao/DaoX.kt` — `@Dao interface DaoX : DaoBase<XEntity>`; `Flow<List<XEntity>>` for observation (with explicit `ORDER BY`), `suspend` for one-shots.
3. Register in `common/data/db/BibliothecaDatabase.kt`: add to `@Database(entities=[...])`, add the `daoX` accessor, **bump `version`** and add a `Migration(n, n+1)` to `addMigrations(...)` when the schema changes (there are real migrations here — never drop data silently; flag the risk).
4. `data/XRepository.kt` — thin `@Inject constructor(db)` delegates.

## 2. Data — remote (Retrofit), only if it syncs

1. `data/api/model/XRequest.kt`/`XResponse.kt` — `@Keep @Serializable`, explicit `@SerialName` per field, nullable + defaulted for fields the backend may omit.
2. `data/api/XApi.kt` — one Retrofit interface **per backend controller** (a distinct controller = a distinct interface, even on the same origin), `suspend` returning `Response<T>` (or a nullable body for GET lists).

## 3. Domain

1. `domain/model/X.kt` — data class with `convertToEntity()`/`convertToUi()`/`convertToRequest()`.
2. `domain/usecase/local/` + `usecase/api/` — one verb per class.
3. `domain/XService.kt` — orchestrates; the ViewModel-facing API.

## 4. UI

1. `ui/<screen>/model/XScreenState.kt` — sealed `Loading`/`Success`/`Failed`.
2. `ui/<screen>/XViewModel.kt` — `@HiltViewModel : BaseViewModel`, `MutableStateFlow(Loading)`, `init(id)` using `Flow.observe(...)`.
3. `ui/<screen>/X.kt` — the screen + a `@Preview` in `VerborumTheme` (see `material-design` for component/theming rules).
4. `ui/<screen>/composables/` — stateless children; hoist state, pass lambdas down.

## 5. Navigation (app module)

Add `SCREEN_X`, an `insertX(...)` extension, and wire it into `NavigationCentral`.

## 6. Resources

- Strings: module `res/values/string.xml` (singular filename in bibliotheca), **camelCase** names, via `stringResource(ResStrings.name)`. **Every user-facing string must be added to all 19 `values-XX/` locales in sync** (see `android-dev`).
- Drawables: hand-written vector XML in `res/drawable/`, `ic_<name>_<size>.xml`.

## 7. Tests & fixtures

Add `testX*` factories to `TestFixtures.kt`; write unit tests per `android-unit-test`.

## 8. Verify

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :<module>:testDebugUnitTest :app:assembleDebug
```

Both must pass before the feature is done.
