# 🍎 Apple TV–style Launcher for Android TV

[![Build Android APK](https://github.com/Real3rr0r303YT/appletv-launcher/actions/workflows/build.yml/badge.svg)](https://github.com/Real3rr0r303YT/appletv-launcher/actions/workflows/build.yml)
[![Latest release](https://img.shields.io/github/v/release/Real3rr0r303YT/appletv-launcher?include_prereleases)](https://github.com/Real3rr0r303YT/appletv-launcher/releases)
[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-Donate-orange.svg?style=for-the-badge&logo=buy-me-a-coffee)](https://buymeacoffee.com/3rr0r303YT)

A custom home screen for Android TV / Google TV with the look and feel of tvOS, written in Kotlin and
Jetpack Compose. Built and tested for TCL TVs, works on any Android TV device running Android 9 or newer.

👉 **[Support the project: buymeacoffee.com/3rr0r303YT](https://buymeacoffee.com/3rr0r303YT)**

## Features

- **Top Shelf** – full-width hero area for the focused app with a blurred banner, colour gradient,
  parallax scrolling and smooth crossfades
- **App grid** – 16:9 rounded tiles; the first row always holds your most recently used apps
- **tvOS focus effect** – the focused tile springs forward, casts a deep shadow, gently sways and gets a
  gloss sweep and a bright rim
- **Remote-first navigation** – smooth D-pad navigation, focus is restored automatically and never gets stuck
- **All your apps** – Android TV apps, phone apps and system Settings, updated live when apps are
  installed, updated or removed
- **Proper home app** – registers for `HOME`, BACK never closes the launcher, HOME/BACK jump back to the top

## Install

1. Download `TVLauncher-<version>.apk` from the [latest release](https://github.com/Real3rr0r303YT/appletv-launcher/releases).
2. Install it on the TV, for example with one of these:
   - **ADB** (TV and computer on the same network):
     ```sh
     adb connect <tv-ip>:5555
     adb install -r TVLauncher-<version>.apk
     ```
   - **USB stick** with a file manager on the TV, or the **Downloader** app with the release URL.

   Allow installing from unknown sources for the app you use (*Settings → Privacy → Security & restrictions*;
   the exact path differs between models).

### Enable ADB on a TCL / Google TV

1. *Settings → System → About* → click **Android TV OS build** 7 times to unlock developer options.
2. *Settings → System → Developer options* → enable **USB debugging** (and **Network debugging** if listed).
3. Find the TV's IP address under *Settings → Network & Internet*.
4. Run `adb connect <tv-ip>:5555` and confirm the prompt on the TV.

## Set as default launcher

Press **HOME** on the remote and pick *TV Launcher*. Many Google TV devices (including most TCL models) do
not show this chooser. Then set it via ADB:

```sh
adb shell cmd package set-home-activity com.custom.appletvlauncher/.MainActivity
```

If the stock launcher still takes over the HOME button, it can be disabled for the current user
(**at your own risk** – some system features such as the Google TV recommendations stop working):

```sh
# Google TV
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
# Classic Android TV
adb shell pm disable-user --user 0 com.google.android.tvlauncher
```

Undo at any time:

```sh
adb shell pm enable com.google.android.apps.tv.launcherx   # or com.google.android.tvlauncher
```

## Remote control

| Button          | Action                                                      |
|-----------------|-------------------------------------------------------------|
| D-pad           | Move between apps                                           |
| OK / Center     | Open the focused app                                        |
| BACK            | Jump back to the first app (the launcher never closes)      |
| HOME            | Return to the launcher; pressed again, jump to the top      |

## Build from source

Requirements: JDK 17 and the Android SDK (API 34). The Gradle wrapper downloads everything else.

```sh
./gradlew assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
```

Stack: Kotlin 1.9.22 · AGP 8.2.2 · Gradle 8.5 · Compose BOM 2024.02.00 · TV Material 3 1.0.0 ·
min SDK 28 · target SDK 34.

## CI and releases

GitHub Actions ([`build.yml`](.github/workflows/build.yml)) builds a debug and a release APK on every
push and pull request and uploads both as workflow artifacts.

A GitHub release with the APK attached is published when

- a tag like `v1.2.0` is pushed: `git tag v1.2.0 && git push origin v1.2.0`, or
- the workflow is started manually (*Actions → Build Android APK → Run workflow*) with a version.

### Release signing

Without signing secrets the release APK is signed with a throwaway debug key generated on the CI runner.
That key changes on every run, so a new version **cannot be installed over an old one** (uninstall first).
To get stable, updatable releases add your own key:

1. Create a keystore once (keep it and the passwords safe – they cannot be recovered):
   ```sh
   keytool -genkeypair -v -keystore release.jks -alias tvlauncher -keyalg RSA -keysize 4096 -validity 10000
   base64 -w0 release.jks > release.jks.b64
   ```
2. Add these repository secrets under *Settings → Secrets and variables → Actions*:

   | Secret                    | Value                               |
   |---------------------------|-------------------------------------|
   | `SIGNING_KEYSTORE_BASE64` | contents of `release.jks.b64`       |
   | `SIGNING_STORE_PASSWORD`  | keystore password                   |
   | `SIGNING_KEY_ALIAS`       | `tvlauncher` (the alias used above) |
   | `SIGNING_KEY_PASSWORD`    | key password                        |

For local signed builds set the same names as environment variables, with `SIGNING_KEYSTORE_PATH`
pointing to the `.jks` file.

## Project structure

```
app/src/main/java/com/custom/appletvlauncher/
├── MainActivity.kt                 Home activity, HOME/BACK handling, focus safety net
├── data/
│   ├── AppInfo.kt                  App model (banner, icon, accent colours, launch intent)
│   ├── AppRepository.kt            Loads TV/phone apps + Settings, listens for package changes
│   └── RecentAppsStore.kt          Recently launched apps (SharedPreferences)
└── ui/
    ├── home/
    │   ├── HomeViewModel.kt        App list, ordering (recent first), launching
    │   └── HomeScreen.kt           Top Shelf + grid, focus logic
    ├── components/
    │   ├── TopShelf.kt             Hero area with blur/gradient and crossfade
    │   ├── AppTile.kt              Grid tile with focus effect
    │   └── AppArtwork.kt           Banner, or icon on a gradient
    ├── focus/
    │   ├── TvOsFocusEffect.kt      Scale, shadow, sway and gloss on focus
    │   └── GridFocusController.kt  Focuses any tile, including off-screen ones
    └── theme/Theme.kt              Colours, typography, tile shape
```

## Troubleshooting

- **Pressing HOME still opens the stock launcher** – see [Set as default launcher](#set-as-default-launcher).
- **`INSTALL_FAILED_UPDATE_INCOMPATIBLE`** – the installed APK was signed with a different key.
  Uninstall it first: `adb uninstall com.custom.appletvlauncher`.
- **An app is missing** – only apps with a launcher entry (TV or phone) are listed.
- **No blur in the Top Shelf** – blur needs Android 12+; older TVs show a softer, unblurred banner instead.
- **Crash logs** – `adb logcat --pid=$(adb shell pidof com.custom.appletvlauncher)`

## Disclaimer

This is an independent fan project. It is not affiliated with, endorsed by or sponsored by Apple Inc.
Apple, Apple TV and tvOS are trademarks of Apple Inc. No Apple assets, fonts or logos are included.

## License

GPL-3.0 – see [LICENSE](LICENSE).
