---
name: material-design
description: Material 3 + Jetpack Compose UI conventions for the Verborum app — theming through VerborumTheme/colorScheme, typography, the shared component patterns (top bar, bottom nav, buttons, FilterChip, ModalBottomSheet, Scaffold), light/dark + RTL support, and accessibility. Load when building or reviewing any Compose screen or composable.
---

# Material Design & Compose (Verborum)

Compose-only, Material 3, no XML layouts. Match the existing look and the theming discipline below.

## Theming — never hardcode colors

- Colors come from **`MaterialTheme.colorScheme`** (`primary`/`onPrimary`, `secondary`, `surface`/`surfaceVariant`/`onSurfaceVariant`, `background`/`onBackground`, `error`/`onErrorContainer`, `outline`, `scrim`). The palette is defined once in `core/theme/` (`VerborumTheme`, `VerborumColors`).
- **Every screen and `@Preview` is wrapped in `VerborumTheme { }}`.** Previews use `@PreviewLightDark` so both themes are checked.
- A raw `Color(0x…)` or hardcoded hex in a composable is a defect — thread it through the theme instead. The one deliberate exception pattern is a fixed on-brand element (e.g. white icon on the secondary swatch); keep those rare and intentional.
- **Pair selection/emphasis colors with their `on` role**: a selected chip that uses `primary` as its container uses `onPrimary` for its label (this is how selected `FilterChip`s match the Create button).

## Typography & spacing

- Sizes are `.sp`, spacing/sizes are `.dp`, via relative `Modifier` composition. Reuse the established scale (screen title ≈ 28.sp bold, body ≈ 14–16.sp, small/meta ≈ 12–13.sp `onSurfaceVariant`).
- Consistent corner radii (`RoundedCornerShape(16.dp)` for cards/buttons) and `Arrangement.spacedBy(...)` for gaps rather than manual `Spacer`s inside rows.

## Shared component patterns (reuse, don't reinvent)

- **Top bar**: screens don't draw their own header — they call `RegisterTopBar(title, subtitle?, showBackButton, action?)` and the single `Scaffold` in `NavigationCentral` renders it. Tab roots pass `showBackButton = false`; deep screens pass `true`. A right-side icon is a `VerborumTopBarAction`.
- **Bottom navigation**: tab roots only, driven by the `screenGroups` list; add a tab by adding a `ScreenGroups` object + nav graph node, not by editing the bar.
- **Buttons**: `Button` (filled `primary`) for the primary action, `OutlinedButton` for secondary; destructive actions use `error` coloring.
- **Chips**: `FilterChip` for multi-select (tags), colored via `FilterChipDefaults.filterChipColors(selectedContainerColor = …, selectedLabelColor = …)`.
- **Bottom sheets**: `ModalBottomSheet` for contextual choices (options, language/sort pickers) — see `SelectionBottomSheet`.
- **Feedback**: transient messages via the shared snackbar (`LocalSnackbarHostState` + `ShowSnackbarMessages`), standing conditions (offline) via a pinned banner. Load failures use `ScreenError`.
- **State-driven UI**: render per sealed state; give lists a stable `key` and a skeleton/loading state so a background sync doesn't visibly reshuffle them.

## Light/dark & RTL

- Support **both** themes — never assume light; test previews in both.
- The app ships **Arabic and Farsi**, so layouts must survive **RTL**: use `start`/`end` padding (not `left`/`right`), lean on Compose's automatic mirroring, and avoid hardcoded directional assumptions.

## Accessibility (non-negotiable)

- Every actionable icon/button has a meaningful `contentDescription` (a resource string, localized) — decorative-only icons use `null` explicitly.
- Touch targets ≥ 48.dp; don't shrink `IconButton` hit areas.
- Text is scalable (`.sp`), contrast comes from correct `on*` color pairing.
- All visible text is a localized resource (see **android-dev** for the 19-locale rule) — no literal user-facing strings in composables.

## State & recomposition hygiene

- Hoist state; pass immutable data down and events up as lambdas. Stateless children live in `ui/<screen>/composables/`.
- `remember`/`remember(key)` for derived/expensive values; `collectAsState` for flows. Don't do work (allocations, `stringResource` in a non-composable lambda) inside hot recomposition paths — resolve labels in a plain loop in the composable body.
