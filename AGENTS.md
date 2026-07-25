# JAViewer — Agent Guide

## What this is

Single-module Android app (Java 17). Browses online movie metadata via configurable data sources. No DI framework, no MVVM — traditional Activity/Fragment architecture with static singletons.

## Build & run

```bash
# Build debug APK (requires Android SDK)
./gradlew assembleDebug        # macOS/Linux
gradlew.bat assembleDebug      # Windows

# Run unit tests (only placeholder tests exist)
./gradlew testDebugUnitTest

# Clean
./gradlew clean
```

AGP 8.9.0, Gradle 8.13, compileSdk 35, minSdk 21, NDK arm64-v8a only.

## Architecture

**Entry flow:** `StartActivity` (LAUNCHER) → loads `assets/properties.json` → initializes `Configurations` from external storage → launches `MainActivity`.

**Singletons** live as static fields on `JAViewer.java` (Application class):
- `HTTP_CLIENT` — shared OkHttpClient with UA spoofing and CSRF token injection
- `SERVICE` — Retrofit `BasicService` instance, rebuilt when data source changes
- `CONFIGURATIONS` — persisted favorites + active data source
- `DATA_SOURCES` — loaded from `properties.json` at startup
- `hostReplacements` — maps legacy domains to current domain

**Network layer:** Retrofit POST to `BasicService` endpoints (`getMovies`, `getMovie`, `search`, etc.). Response bodies are raw `ResponseBody` — parsed manually in `AVMOProvider` and similar provider classes.

**Package layout** under `io.github.javiewer`:
- `activity/` — Activities (Start, Main, Movie, Gallery, Download, WebView, MagnetSearch, Favourite)
- `fragment/` — Fragments (Home, Popular, Released, Actresses, Genre, Movie, Download, Favourite tabs)
- `adapter/` — RecyclerView adapters + data models in `adapter/item/`
- `network/` — Retrofit interfaces (BasicService, Avgle, TorrentKitty, BTSO) and JSON providers
- `view/` — Custom views and scroll listeners
- `util/` — IO and video player utilities

## Gotchas

- **All UI strings are hardcoded in Chinese** in Java source and layouts — no `strings.xml`. Do not add string resources; match the existing pattern.
- **No real tests** — only `ExampleUnitTest` (placeholder). Do not expect test coverage to catch regressions.
- **ProGuard is off** for release builds (`minifyEnabled false`).
- **Cleartext traffic permitted** in `network_security_config.xml` — intentional for the data sources.
- **Config persistence** writes to external storage (`/sdcard/JAViewer/configurations.json`). The app creates a `.nomedia` file there.
- **`SecureActivity`** sets `FLAG_SECURE` on pause, clears on resume — prevents screenshots. `MainActivity` extends it.
- **CSRF token** is fetched asynchronously on `MainActivity.onCreate`. Network calls may fail if they race with token fetch.
- **Domain switching** rebuilds the Retrofit service and recreates the Activity. The `hostReplacements` map redirects legacy domains to the active domain.
- **`android.nonTransitiveRClass=false`** in gradle.properties — R class references are transitive.

## Key files

- `app/src/main/assets/properties.json` — data sources config (domains, API paths, legacy domain list)
- `JAViewer.java` — Application singleton, holds all global state
- `Configurations.java` — persistent user config (favorites, active source)
- `StartActivity.java` — app startup, permission check, config init
- `BasicService.java` — Retrofit API interface
- `AVMOProvider.java` — JSON response parser (movies, actresses, genres, details)
