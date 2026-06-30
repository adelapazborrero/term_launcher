# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HackerLauncher is an Android home screen replacement (launcher) with a terminal/hacker aesthetic. Users interact entirely through a command prompt — no icons, no widgets. The input bar is always pinned at the bottom; output scrolls upward like a terminal session.

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected USB device and launch
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests on device
./gradlew connectedAndroidTest

# Check for lint issues
./gradlew lint

# Build + install in one step (most common during dev)
./gradlew installDebug && adb shell am start -n com.hackerlauncher/.LauncherActivity
```

Requires Android SDK installed and `ANDROID_HOME` set (or `local.properties` with `sdk.dir`). `adb` is not on PATH — use the full path: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.

### Connecting the Test Device (Xiaomi Redmi, WiFi ADB)

USB install is blocked by MIUI. Use WiFi ADB instead:

1. On phone: **Settings → Additional Settings → Developer Options → Wireless debugging → ON**
2. Also enable **"Install via USB"** in Developer Options (required even for WiFi installs on MIUI)
3. Tap **"Pair device with pairing code"** — note the IP, pairing port, and 6-digit code
4. Run: `adb pair <ip:pairingPort> <code>`
5. Back on the Wireless debugging main screen, note the **connection port** (different from pairing port)
6. Run: `adb connect <ip:connectionPort>`
7. Verify with: `adb devices`

The device IP is typically `192.168.2.81`. Pairing codes expire quickly — run `adb pair` immediately after getting the code.

After connecting, use `.\gradlew installDebug` (Windows) to build and install.

## Architecture

### Entry Point & Home Screen Registration

`LauncherActivity` is the single Activity. The `AndroidManifest.xml` declares it with both `CATEGORY_HOME` and `CATEGORY_DEFAULT` so Android offers it as a launcher choice. On first install, the user is prompted to set it as default.

### Terminal UI Model

The screen is split into two parts:
- **Output area** (`RecyclerView`): scrollable list of `TerminalEntry` items (user input lines, command output lines, error lines). New entries append to the bottom; the list auto-scrolls.
- **Input bar** (pinned `LinearLayout` at bottom): a static prompt string (e.g. `root@device:~$`) followed by an `EditText`. The keyboard is always shown.

`TerminalAdapter` binds `TerminalEntry` items. Each entry has a `type` enum (`INPUT`, `OUTPUT`, `ERROR`, `INFO`) that controls text color.

### Command Processing

`CommandProcessor` receives the raw input string, tokenizes it, and dispatches to a `Command` implementation via a `Map<String, Command>`. Each `Command` is a single-method interface:

```kotlin
interface Command {
    fun execute(args: List<String>, context: CommandContext): List<TerminalEntry>
}
```

`CommandContext` carries references to `AppManager`, `ThemeManager`, and the Android `Context` so commands can act on the system without coupling to the Activity.

Commands are registered in `CommandRegistry` — adding a new command means implementing `Command` and registering it there. No reflection, no annotations.

### App Management

`AppManager` wraps `PackageManager` queries. It caches the installed app list (`List<AppInfo>`) and refreshes it on `ACTION_PACKAGE_ADDED` / `ACTION_PACKAGE_REMOVED` broadcasts. `AppInfo` is a plain data class holding label, package name, and launch intent.

`open`/`launch` commands use fuzzy matching (Levenshtein or simple `contains` ranking) against app labels so users don't need exact names.

### Theme System

`ThemeManager` holds the active `HackerTheme` (data class with foreground color, background color, prompt color, error color, font size). Themes are predefined constants in a companion object. Switching themes posts to a `StateFlow` that `LauncherActivity` collects, triggering a full UI refresh without recreating the Activity.

## Key Files

| File | Purpose |
|---|---|
| `LauncherActivity.kt` | Single activity; owns the RecyclerView + input bar; collects theme + settings StateFlows |
| `LauncherViewModel.kt` | Owns all managers; exposes `entries`, `currentTheme`, `currentSettings` StateFlows |
| `commands/CommandProcessor.kt` | Tokenizes input, resolves aliases, dispatches to Command implementations |
| `commands/CommandRegistry.kt` | Map of command name → Command; only place to register new commands |
| `commands/Command.kt` | `Command` interface |
| `commands/CommandContext.kt` | Carries all managers into commands |
| `commands/AliasManager.kt` | Persists aliases (name → expansion) in SharedPreferences |
| `apps/AppManager.kt` | PackageManager wrapper; app list cache; fuzzy search |
| `apps/FavoritesManager.kt` | Persists favorite package names in SharedPreferences |
| `settings/SettingsManager.kt` | Persists font_size and prompt; exposes StateFlow |
| `settings/Settings.kt` | Data class for user-configurable settings |
| `theme/ThemeManager.kt` | Theme state; persists active theme; StateFlow publisher |
| `theme/HackerTheme.kt` | Theme data class + predefined themes (matrix, blood, ice, amber, ghost) |
| `terminal/TerminalAdapter.kt` | RecyclerView adapter for terminal output; applies theme + font size |
| `terminal/TerminalEntry.kt` | Data class for a single terminal line (INPUT/OUTPUT/ERROR/INFO) |

## Built-in Commands

| Command | Description |
|---|---|
| `open <name>` / `launch <name>` | Fuzzy-match and launch an installed app; checks favorites first |
| `apps [filter]` / `ls` | List all installed apps, optional name filter |
| `fav add <name>` | Add an app to favorites |
| `fav remove <name>` | Remove an app from favorites |
| `fav list` | List favorite apps |
| `alias <name> <cmd>` | Create a persistent alias (`alias br "open brave"`) |
| `alias list` | List all aliases |
| `alias remove <name>` | Remove an alias |
| `settings list` | Show all settings (theme, font_size, prompt) |
| `settings set theme <name>` | Switch color theme (matrix, blood, ice, amber, ghost) |
| `settings set font_size <8-32>` | Change text size in sp |
| `settings set prompt <text>` | Change the prompt string |
| `settings reset` | Reset all settings to defaults |
| `themes` | List available themes |
| `clear` / `cls` | Clear terminal output |
| `help [command]` | Show all commands or help for a specific command |
| `info` | Show device info (model, Android version, battery, time) |

## Adding a New Command

1. Create `commands/impl/MyCommand.kt` implementing `Command`
2. Register it in `CommandRegistry` with its name string
3. Add a `description` property — `HelpCommand` reads descriptions from the registry

## Android-Specific Notes

- `LauncherActivity` must call `finish()` carefully — pressing Back on a launcher should not exit; intercept `onBackPressed` to no-op or show a "press again to show app drawer" hint.
- `FLAG_ACTIVITY_NEW_TASK` is required when starting other apps from a launcher context.
- The soft keyboard should never be dismissed; use `windowSoftInputMode="adjustResize"` so the layout shrinks rather than the keyboard hiding.
