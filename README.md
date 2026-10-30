# 🍎 Apple TV Custom Launcher for Android TV

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-Donate-orange.svg?style=for-the-badge&logo=buy-me-a-coffee)](https://buymeacoffee.com/3rr0r303YT)

👉 **[Hier spenden: buymeacoffee.com/3rr0r303YT](https://buymeacoffee.com/3rr0r303YT)**

## Aufbau

```
app/src/main/java/com/custom/appletvlauncher/
├── MainActivity.kt              Home-Activity, HOME/BACK-Handling, Fokus-Sicherheitsnetz
├── data/
│   ├── AppInfo.kt               Modell einer App (Banner, Icon, Akzentfarben, Launch-Intent)
│   ├── AppRepository.kt         Liest alle TV-/Phone-Apps + Einstellungen, reagiert auf (De-)Installationen
│   └── RecentAppsStore.kt       Zuletzt gestartete Apps (SharedPreferences)
└── ui/
    ├── home/
    │   ├── HomeViewModel.kt     App-Liste, Reihenfolge (zuletzt genutzt zuerst), App-Start
    │   └── HomeScreen.kt        Top Shelf + App-Grid, Fokus-Logik
    ├── components/
    │   ├── TopShelf.kt          Hero-Bereich mit Blur/Gradient und Crossfade
    │   ├── AppTile.kt           App-Kachel mit Fokus-Effekt
    │   └── AppArtwork.kt        Banner bzw. Icon auf Farbverlauf
    ├── focus/
    │   ├── TvOsFocusEffect.kt   Scale, Schatten, Neigen und Glanz bei Fokus
    │   └── GridFocusController.kt  Fokus auf beliebige (auch nicht sichtbare) Kacheln setzen
    └── theme/Theme.kt           Farben, Typografie, Kachelform
```

## Als Standard-Launcher setzen

Nach der Installation die HOME-Taste drücken und den Launcher auswählen. Auf Google-TV-Geräten
(viele TCL-Modelle) fehlt diese Auswahl oft; dann per ADB:

```
adb shell cmd package set-home-activity com.custom.appletvlauncher/.MainActivity
```
