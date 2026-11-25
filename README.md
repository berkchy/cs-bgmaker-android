# BG Maker (Android)

An open-source Material 3 Android app that turns any photo into a **CS 1.6 background**:
a 1600×720 (by default) canvas cut into 32-bit TGA tiles plus the
`BackgroundLayout.txt` / `BackgroundLoadingLayout.txt` files. It is a mobile port of
`cs-bgmaker` and keeps the original conversion rules, naming and output layout.

## Features

- **Photo picker** using the Android system Photo Picker (no storage permission required),
  plus an optional recent-images strip backed by MediaStore.
- **Live layout preview** of the 256 px tile grid before conversion.
- **Resolution presets** (1600×720, 1920×1080, 1366×768, 1024×768, 800×600) and custom sizes.
- **Resize modes**: `cover` (crop to fill), `fit` (letterbox), `stretch`.
- **32-bit TGA writer** producing bottom-up, type-2, BGRA tiles (`bg_0_0.tga` …).
- **Layout generation** matching the original `resource/background/... scaled x y` format.
- **Backup / restore** of previous output into `_backup_<timestamp>` folders.
- **Export** a ZIP of the result with Android Share Sheet or Save-as via SAF.
- **Material You**: light/dark/system themes, optional dynamic color, and Google Sans typography.

## Architecture

The Gradle project mirrors the module layout of the Python source. Pure conversion logic
is isolated in `:core`; all Android UI, storage and state live in `:app`.

| Python module | Android counterpart | Responsibility |
|---|---|---|
| `config.py` | `core/Grid.kt`, `core/Models.kt` | Grid math, presets, config models |
| `converter.py` | `core/Converter.kt` | Resize (cover/fit/stretch), tile crop, TGA encoding |
| `layout.py` | `core/LayoutWriter.kt` | Layout text generation |
| `backup.py` | `core/BackupManager.kt` | Backup, list, restore, delete |
| `cli.py` / `app.py` orchestration | `core/ConversionEngine.kt` | Progress-reporting pipeline |
| `picker.py` | `ui/picker/PickerScreen.kt`, `util/ImageLoader.kt` | Image selection and decoding |
| `config.py` settings | `data/SettingsRepository.kt` | Persisted preferences |
| `utils.py` / `cli.py` output | `ui/result/*`, `util/OutputHelper.kt` | Progress, errors, ZIP/share |
| — | `ui/configure/*`, `ui/settings/*` | Layout/conversion controls, backups UI |

```
cs-bgmaker-android/
├── core/                       # conversion engine (no UI dependencies)
│   └── src/main/java/dev/berkchy/bgmaker/core/
│       ├── Grid.kt             buildGrid(width, height, step = 256)
│       ├── Models.kt           Resolution, ResizeMode, config/result models
│       ├── Converter.kt        prepare(), tile(), writeTga()
│       ├── LayoutWriter.kt     build()/write() of both layout files
│       ├── BackupManager.kt    backup()/restore()/list()
│       └── ConversionEngine.kt orchestration + progress callback
└── app/                        # Material 3 UI
    └── src/main/java/dev/berkchy/bgmaker/
        ├── MainActivity.kt
        ├── data/               SharedPreferences repository
        ├── theme/              Color, Type (Google Sans), Shape, Theme
        ├── ui/                 Navigation, ViewModel
        │   ├── picker/         Selection + preview
        │   ├── configure/      Settings + live grid preview
        │   ├── result/         Progress + result
        │   └── settings/       Preferences + backup manager
        └── util/               Image decoding, ZIP/share helpers
```

## Build

Debug and release APKs are built by Gradle (AGP 8.7.3, Kotlin 2.0.20, Compose BOM
2024.10.01, `compileSdk 35`, `minSdk 28`).

```bash
# Local
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
# outputs: app/build/outputs/apk/{debug,release}/*.apk
```

CI does the same on every push/PR via `.github/workflows/android.yml` and uploads both
APKs as build artifacts. Release builds are signed with the debug key unless a
`keystore.properties` is present at the repository root:

```properties
storeFile=release.jks
storePassword=...
keyAlias=bgmaker
keyPassword=...
```

## Usage

1. **Pick** an image from the gallery (or the recent strip).
2. **Configure** resolution, resize mode, folder, tile prefix and backup behavior. The
   grid preview updates instantly.
3. **Convert**. Progress is shown step by step; any failure is reported in place.
4. **Export** the ZIP via Share or Save-as, or manage old outputs in **Settings → Backups**.

Default output is `Documents/BGMaker/1600x720/` in app-specific external storage (no
storage permission needed). Output contains:

```
1600x720/
├── background/                     # 21 × 32-bit TGA tiles
├── BackgroundLayout.txt
├── BackgroundLoadingLayout.txt
└── _backup_<yyyyMMdd-HHmmss>/       # previous tiles/layouts, when backup is on
```

Copy `background/` into the game's `resource/` folder and place both layout files next to it.

## GitHub / `gh` workflow

The `gh` CLI is assumed to be authenticated (`gh auth status`). From this directory:

```bash
git init
git add .
git commit -m "Initial release of BG Maker for Android"
gh repo create cs-bgmaker-android --public --source=. --remote=origin --push
git push -u origin HEAD
```

Subsequent pushes trigger the Android CI workflow automatically. Download the APKs from
the Actions run's artifact list or from `gh run view --web`.

## Fonts

`app/src/main/res/font/google_sans_regular.ttf` contains **Google Sans** from the
[googlefonts/googlesans](https://github.com/googlefonts/googlesans) project (Apache 2.0).
The regular weight is the default family for every typography style in `theme/Type.kt`.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
