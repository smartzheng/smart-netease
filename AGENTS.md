# Repository guidance

This repository maintains the Smart Netease Android application only. It is a fork of [GuitaristRin/Ncrust](https://github.com/GuitaristRin/Ncrust) that has since evolved substantially. Keep project attribution and license notices intact; see [`docs/PROJECT.md`](docs/PROJECT.md).

## Build

- Android application module: `:app`.
- Android Macrobenchmark module: `:benchmark`.
- `./gradlew :app:assembleDebug` builds a debug APK.
- `./gradlew :app:assembleRelease` builds an optimized release APK and requires local signing configuration.
- `./gradlew test` runs JVM tests; `./gradlew connectedAndroidTest` requires an Android device or emulator.
- Use JDK 21 and Android SDK Platform 36. The app supports API 24 and later.

On Windows development hosts, use the checked-in `gradlew.bat` wrapper.

## Architecture

- App ID and namespace: `com.smart.neteast`.
- Kotlin and Jetpack Compose provide the app and UI. Salt UI is the current Compose component library; do not add a second UI toolkit.
- AndroidX Media3 handles playback and media sessions.
- `network/` contains the NetEase API clients and request protocols; `player/` contains playback service and state; `library/` manages cloud library state; `ui/` contains screens, components, design tokens and navigation.
- `spec/api/` documents Android protocol behavior. `spec/fixtures/` contains data read by Android tests; update a fixture together with its corresponding implementation and test.
- `benchmark/` contains Android Macrobenchmark scenarios.

## UI changes

- Reuse existing Salt UI primitives, app-owned design tokens and shared components under `ui/design/` and `ui/theme/`.
- Preserve adaptive layouts, status/navigation bar insets, accessibility labels and touch targets.
- Player expand/collapse and cover motion are centralized in `ui/player/`; avoid introducing a second competing animation state.
- Keep pressed-state effects transient and avoid persistent decorative shadows.

## Releases

- `app/build.gradle.kts` is the source of truth for `versionName` and `versionCode`.
- Keep signing material out of Git. Never commit keystores, passwords, cookies or account data.
- Release APKs are published in GitHub Releases with notes describing user-visible changes.
- Confirm the release APK signature before attempting to replace an installed app. Uninstalling an app can erase its local data.

## Commits

Use Conventional Commit prefixes and concise Chinese subjects, for example `fix: 修复歌词顶栏对齐`. Keep app changes, documentation changes and unrelated cleanup in coherent commits.
