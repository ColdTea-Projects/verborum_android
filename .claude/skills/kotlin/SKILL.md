---
name: kotlin
description: Kotlin language conventions and best practices as applied in the Verborum Android app — immutability and data/sealed classes, null-safety, coroutines and Flow, structured concurrency and dispatchers, expression bodies and scope functions, kotlinx.serialization, and the idioms this codebase standardizes on. Load when writing or reviewing Kotlin regardless of layer.
---

# Kotlin (Verborum)

Idiomatic, defensive Kotlin. These are the conventions the codebase already follows — match them.

## Types & immutability

- **Prefer `val` and immutable data.** Models are `data class`es with `val` fields; produce new state with `.copy(...)` rather than mutating. Expose `List`, not `MutableList`.
- **`data class`** for models/DTOs (auto `equals`/`copy` — the sync layer relies on structural equality to skip no-op writes). **`sealed class`/`sealed interface`** for closed hierarchies: UI state (`Loading`/`Success`/`Failed`), `UiText` (`Resource`/`Dynamic`), tag kinds. Use `data object` for stateless variants.
- **Enums** for fixed small sets with behavior (`SupportedLanguage`, `DictionarySort` carrying a `labelRes`).
- Model conversions are **member functions** (`convertToUi()`), field-by-field with **named arguments** — no reflection, no mapper frameworks.

## Null-safety

- Push nullability to the edges (DTOs: nullable + defaulted for fields the backend may omit; resolve to non-null with fallbacks at the boundary).
- Prefer `?.`, `?:`, `?.let { }` over `!!`. `!!` is a smell — if a value is guaranteed, express it in the type.
- `runCatching { … }.getOrNull()/.getOrDefault(...)` for "best-effort, fall back" (used in JWT decode, tag reconcile, JSON parse). Don't use it to swallow errors that should surface to the user.

## Coroutines & Flow

- **Suspend, don't block.** One-shot reads are `suspend fun`; streams are `Flow`. Never block a thread with `runBlocking` in production.
- **Dispatchers**: IO-bound work off the main thread — Services use `.flowOn(Dispatchers.IO)`; use cases that hit DB/disk wrap in `withContext(Dispatchers.IO)`. Retrofit `suspend` functions already switch internally. Don't do disk/network on `Dispatchers.Main`.
- **Structured concurrency**: launch in `viewModelScope` (via `BaseViewModel.observe` / `viewModelScope.launch(exceptionHandler)`); app-lifetime work uses a dedicated scope. Never `GlobalScope`.
- **Flow operators**: `map`/`combine` to shape data, `distinctUntilChanged()` to suppress no-op emissions (place it *after* the UI mapping so domain-only changes don't recompose), `debounce` for burst-collapsing (the pending-upload trigger). `catch { }` for stream error handling.
- Bridge callback APIs with `suspendCancellableCoroutine` (as `AuthManager` does for AppAuth) rather than polling.

## Idioms

- **Expression bodies** for thin delegates (repositories, use cases): `fun getX() = dao.getX()`.
- **Scope functions** by intent: `let` (nullable transform), `apply` (configure-and-return, e.g. OkHttp builder), `also` (side effect), `run`/`with` (grouped calls). Don't nest them into puzzles.
- **Extension functions** for cross-cutting helpers on existing types (`Flow<T>.observe`, `String` surface parsing). **Group them by topic into `extensions/`, one file per logical group, named `<Subject>+<Group>.kt`** — `Context+Connectivity.kt`, `Data+Objects.kt`, `User+Interaction.kt` in `core/extensions/`. Never a catch-all `Extensions.kt`, and never one file per function.
  - Don't leave a helper as a `private fun` at the bottom of the class or composable that happened to need it first. If a second caller could ever want it, it goes in the group file as a public extension — that is why `Context.hasInternet()` / `Context.connectivityManager()` moved out of `ui/ConnectivityState.kt` into `core/extensions/Context+Connectivity.kt`.
  - No matching group file yet? Create one named for the group, not for the caller that prompted it.
- Destructure `partition`/`associateBy`/`groupBy` for set reconciliation (sync diffing) instead of manual loops.
- Multiline strings/URLs and SQL: concatenate readably; keep `@Query` SQL formatted.

## Serialization

- kotlinx.serialization: `@Keep @Serializable` with explicit `@SerialName` on **every** field. Nullable + default for optional/omittable fields. Reuse the module `Json` instance (lenient, `ignoreUnknownKeys`) rather than constructing new ones ad hoc.

## Don'ts

- No `!!` chains, no `lateinit` for things that could be constructor params, no blocking calls on Main, no `GlobalScope`, no catching `Throwable` to hide failures, no mutable shared state without a concurrency guard (`@Synchronized`/`MutableStateFlow`).
