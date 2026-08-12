# Arcana Vault

Arcana Vault is an offline-first Pokémon TCG card binder and deck workspace for Android. It combines a responsive Compose interface with local persistence and a public card API.

## Highlights

- Search and filter Pokémon cards from the TCGdex v2 API
- Room-backed offline cache, favorites, and deck storage
- DataStore preferences for theme, motion, and grid density
- MVI state management with immutable state and one-off effects
- Phone navigation bar and tablet/foldable navigation rail
- List-detail layouts for large and connected displays
- Fold posture awareness through Jetpack WindowManager
- Responsive Glance widget with a bitmap-rendered Pretendard type system
- Five-minute recommended-card refresh through a resilient WorkManager chain
- Dynamic color, dark mode, reduced motion, and edge-to-edge UI
- Animated aurora surfaces and translucent glass styling

## Architecture

The project follows a Now in Android-style convention-plugin and multi-module structure.

```text
app
├── core:common, model, domain, data
├── core:network, database, datastore
├── core:designsystem
├── feature:catalog, deck, favorites, settings
└── widget
```

Room is the local source of truth. Network responses are cached before the UI observes them, while user preferences remain isolated in DataStore. Domain use cases own recommendation rules and feature state follows an Action → Reducer → State/Effect flow.

The widget selects a different cached card when possible. Android may defer the requested five-minute refresh while the device is idle or battery restrictions are active.

## Build

Requirements: Android Studio with JDK 17 and Android SDK 37.1.

```bash
./gradlew test lintDebug assembleDebug
```

## Data source

Card metadata is provided by the [TCGdex API](https://tcgdex.dev/rest). The app caches list responses and loads high-resolution detail only when needed. Pokémon names, artwork, and related properties belong to their respective rights holders.

Pretendard is distributed under the SIL Open Font License 1.1. Its license text is bundled with the design-system resources.

## License

Copyright 2026 woojaeHEO. Licensed under the MIT License.
