# Paging 3 (Verborum)

How a paged list is built here. The canonical example is the Forum browse list:
`forum/.../marketplace/` — `domain/paging/MarketplaceListingPagingSource.kt`,
`MarketplaceService.getDictionaries()`, `ForumDictionaryListViewModel`, `ForumDictionaryList.kt`.

## Dependencies

Catalog aliases (one `paging` version): `androidx.paging.common` (domain `PagingSource`),
`androidx.paging.compose` (`collectAsLazyPagingItems`), `androidx.paging.testing` (tests only).
No `paging-runtime` — that is the RecyclerView adapter, unused in a Compose-only app.

## Layers

| Layer | Owns |
|---|---|
| Repository | one page per call: `getX(page, size)` returning the wire `PageResponse` |
| Use case | `invoke(page, size)`, converting the response to a domain page model (`items`, `page`, `hasMore`) |
| `domain/paging/XPagingSource` | page-number keys, cross-page de-duplication, `LoadResult.Error` on failure |
| Service | the `Pager` + `PagingConfig`; maps `PagingData<X>` → `PagingData<XUi>` with `androidx.paging.map` |
| ViewModel | `val items = service.getX().cachedIn(viewModelScope)` — no sealed `Loading/Success/Failed` state |
| Screen | `collectAsLazyPagingItems()`, branching on `loadState` |

## Rules

- **Page-number keys need a fixed size.** Set `initialLoadSize = pageSize` (Paging's default is
  3× pageSize) and send the constant size, never `params.loadSize` — otherwise page N no longer
  starts at item `N * size` and items repeat or vanish.
- **Append-only + de-dupe.** `prevKey = null`, `getRefreshKey = null` (a refresh restarts at page 0);
  keep a per-source `seenIds` set and filter each page through it. A repeated key crashes `LazyColumn`.
- `enablePlaceholders = false` for backend pages whose total can shift mid-scroll.
- Rethrow `CancellationException` in `load`; every other exception becomes `LoadResult.Error`.
- `cachedIn(viewModelScope)` exactly once, in the ViewModel, so pages survive back-navigation.
- Filters/queries: hold them in a `MutableStateFlow` and build
  `filter.flatMapLatest { service.getX(it) }.cachedIn(viewModelScope)` — each change starts a new
  pager from page 0. Key the screen's list state by the filter
  (`rememberSaveable(filter, saver = LazyListState.Saver)`) so new results start at the top.

## Screen

```kotlin
val items = viewModel.items.collectAsLazyPagingItems()
when {
    items.itemCount > 0 -> LazyColumn(state = listState) {        // listState hoisted
        items(count = items.itemCount, key = items.itemKey { it.id },
              contentType = items.itemContentType { "row" }) { i -> items[i]?.let { Row(it) } }
        // footer: loadState.append Loading → spinner; Error → message + Retry (items::retry)
    }
    items.loadState.refresh is LoadState.Loading -> Skeleton()
    items.loadState.refresh is LoadState.Error -> ScreenError(onRetry = items::retry)
    else -> EmptyState()
}
```

Pull-to-refresh: wrap the column in `PullToRefreshBox(isRefreshing, onRefresh = items::refresh)`
with `isRefreshing = loadState.refresh is Loading && itemCount > 0` (the first load has its
skeleton). A failed refresh keeps the old items, so report it on `LocalSnackbarHostState`.

Previews: `flowOf(PagingData.from(list)).collectAsLazyPagingItems()`.

## Tests (`paging-testing`)

- PagingSource: `TestPager(config, source)` → `refresh()`, `append()`, `getPages()`; assert keys,
  de-duplication, and `LoadResult.Error`.
- Service: `service.getX().asSnapshot()` inside `runTest` returns the loaded items.
- ViewModel: stub the service with `flowOf(PagingData.from(list))`. **Never `asSnapshot()` the
  `cachedIn(viewModelScope)` flow directly** — it never completes and `runTest` times out
  (`UncompletedCoroutinesError`). Snapshot its first emission:
  `flowOf(viewModel.items.first()).asSnapshot()`.
