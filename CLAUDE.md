# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TermLauncher is an Android home screen replacement (launcher) with a terminal/hacker aesthetic. Users interact entirely through a command prompt — no icons, no widgets. The input bar is always pinned at the bottom; output scrolls upward like a terminal session.

The app supports two UI modes switchable at runtime: **terminal** (pure text) and **modern** (rounded input border, color-coded status indicators, timestamps, command dividers).

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
```

Since `installDebug` is blocked on this device, the deploy workflow is:
```bash
.\gradlew assembleDebug
# then push APK manually (see WiFi ADB section below)
$adb -s 192.168.2.81:43235 push app\build\outputs\apk\debug\app-debug.apk /sdcard/Download/app-debug.apk
# user installs from Downloads in file manager
# then launch:
$adb -s 192.168.2.81:43235 shell am start -n com.termlauncher/.LauncherActivity
```

Requires Android SDK installed and `ANDROID_HOME` set (or `local.properties` with `sdk.dir`). `adb` is not on PATH — use the full path: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.

### Connecting the Test Device (Xiaomi Redmi, WiFi ADB)

USB install is blocked by MIUI (requires SIM card to enable). Use WiFi ADB + manual sideload:

1. On phone: **Settings → Additional Settings → Developer Options → Wireless debugging → ON**
2. Tap **"Pair device with pairing code"** — note the IP, pairing port, and 6-digit code
3. Run: `adb pair <ip:pairingPort> <code>`  ← pairing codes expire in ~30 seconds, run immediately
4. Back on Wireless debugging main screen, note the **connection port** (different from pairing port)
5. Run: `adb connect <ip:connectionPort>`
6. Verify with: `adb devices`

Device IP: `192.168.2.81`. After connecting, push the APK and install from the phone's file manager.

## Architecture

### Entry Point & Home Screen Registration

`LauncherActivity` is the single Activity. `AndroidManifest.xml` declares it with two intent filters on the same activity: `CATEGORY_HOME` + `CATEGORY_DEFAULT` (so Android offers it as a launcher choice) and a separate `CATEGORY_LAUNCHER` filter (so it also shows up as a regular app icon in the app drawer of whichever launcher is active). Also declares `QUERY_ALL_PACKAGES` permission and `<queries>` intent filter — required on Android 11+ for `PackageManager` to return all installed apps.

### Terminal UI Model

The screen is split into two parts:
- **Output area** (`RecyclerView`): scrollable list of `TerminalEntry` items. New entries append at the bottom; the list auto-scrolls. `TerminalAdapter` uses three view types depending on the active UI mode.
- **Input bar** (pinned `LinearLayout` at bottom): prompt `TextView` + command `EditText`.

`TerminalEntry` carries: `text`, `type` (INPUT/OUTPUT/ERROR/INFO/DIVIDER), `timestamp` (set in modern mode), `success` (true/false for INPUT entries, drives indicator color).

### UI Modes

Controlled by `settings set ui_mode terminal|modern`. The Activity collects `currentSettings` StateFlow and calls `applySettings()` + `applyInputBarStyle()` live.

**Terminal mode** (default): plain monospace text, `$` prefix on commands, no decorations.

**Modern mode**:
- Input bar gets a rounded rectangle border (10dp corners) in the theme's prompt color
- INPUT entries render with a `◎` indicator (green = `theme.foreground`, red = `theme.error`), command text, and `HH:mm` timestamp right-aligned
- A faint divider line is appended after each command block

### Command Processing

`CommandProcessor.process()` flow:
1. Tokenize input (handles quoted strings: `alias br "open brave"`)
2. Check `AliasManager` — expand alias and re-process (max depth 5 to prevent cycles)
3. Check for `-h` flag — return command description without executing
4. Dispatch to `Command` implementation via `CommandRegistry`

Each `Command` implements:
```kotlin
interface Command {
    val description: String
    fun execute(args: List<String>, context: CommandContext): List<TerminalEntry>
}
```

`CommandContext` carries all managers. `SessionState` (also in context) holds mutable per-session state like `currentFolder`.

### Folder System

Apps can be organized into named folders. `FolderManager` persists folder names and memberships in SharedPreferences using synchronous `commit()` writes (not `apply()`) to ensure data survives process restarts.

- Folder names and app memberships are stored separately: `folder_names` (Set<String>) and `folder_<name>` (Set<String> of package names)
- `"apps"` is a reserved virtual folder name representing uncategorized apps
- `ls` at root shows all folders including `[apps]`; `ls <folder>` shows contents; `cd` navigates the session
- `mv` supports Linux-style paths: `mv apps/Spotify music/`, `mv ../apps/Spotify .`

### App Management

`AppManager` queries `PackageManager` for all apps with `CATEGORY_LAUNCHER`. Caches the list and refreshes on package add/remove/replace broadcasts. `open` checks favorites first, then all apps.

### Persistence

All user data is stored in SharedPreferences and survives app restarts:

| Store | File | Contents |
|---|---|---|
| `ThemeManager` | `"theme"` | Active theme name |
| `SettingsManager` | `"app_settings"` | font_size, prompt, ui_mode |
| `FavoritesManager` | `"favorites"` | Favorite package names |
| `FolderManager` | `"app_folders"` | Folder names + memberships |
| `AliasManager` | `"aliases"` | Alias name → expansion |

## Key Files

| File | Purpose |
|---|---|
| `LauncherActivity.kt` | Single activity; RecyclerView + input bar; collects theme + settings StateFlows; applies UI mode |
| `LauncherViewModel.kt` | Owns all managers + SessionState; exposes `entries`, `currentTheme`, `currentSettings` |
| `commands/CommandProcessor.kt` | Tokenizes, resolves aliases, handles `-h`, dispatches |
| `commands/CommandRegistry.kt` | Single place to register commands |
| `commands/CommandContext.kt` | Passes all managers + session into commands |
| `commands/SessionState.kt` | Mutable per-session state (currentFolder) |
| `commands/AliasManager.kt` | Persists aliases in SharedPreferences |
| `apps/AppManager.kt` | PackageManager wrapper; app list cache; fuzzy search |
| `apps/FavoritesManager.kt` | Persists favorite package names |
| `apps/FolderManager.kt` | Persists folders and app memberships; atomic commit() writes |
| `settings/Settings.kt` | `Settings` data class + `UiMode` enum |
| `settings/SettingsManager.kt` | Persists settings; exposes `StateFlow<Settings>` |
| `theme/ThemeManager.kt` | Active theme; persists name; exposes `StateFlow<HackerTheme>` |
| `theme/HackerTheme.kt` | Theme data class + predefined themes: matrix, blood, ice, amber, ghost |
| `terminal/TerminalAdapter.kt` | RecyclerView adapter; 3 view types: text, modern-input, divider |
| `terminal/TerminalEntry.kt` | Data class: text, type, timestamp, success |

## Built-in Commands

Every command supports `-h` for inline help (handled globally in `CommandProcessor`).

| Command | Description |
|---|---|
| `open <name>` / `launch <name>` | Fuzzy-match and launch an app; checks favorites first |
| `apps [-f <filter>]` | List all installed apps; `-f` filters by name |
| `ls [folder]` | List folders at root, or contents of a folder |
| `cd <folder\|..>` | Enter a folder or return to root; supports `cd apps` |
| `mkdir <name>` | Create an app folder (`apps` is reserved) |
| `mv <app\|path> <folder\|.\|..>` | Move app to folder; supports paths like `apps/Spotify music/` |
| `fav add <name>` | Add app to favorites |
| `fav remove <name>` | Remove app from favorites |
| `fav list` | List favorites |
| `alias <name> <cmd>` | Create persistent alias: `alias br "open brave"` |
| `alias list` | List all aliases |
| `alias remove <name>` | Remove an alias |
| `settings list` | Show all settings |
| `settings set <key> <value>` | Change a setting (see keys below) |
| `settings reset` | Reset all settings to defaults |
| `themes` | List available themes |
| `clear` / `cls` | Clear terminal output |
| `help` | Bullet list of command names |
| `help -l` | Verbose help with descriptions |
| `help <cmd>` | Help for a specific command |
| `info` | Device info (model, Android version, battery, time) |

### Settings Keys

| Key | Values | Default |
|---|---|---|
| `theme` | matrix, blood, ice, amber, ghost | matrix |
| `ui_mode` | terminal, modern | terminal |
| `font_size` | 8–32 (sp) | 14 |
| `prompt` | any string | `root@hackr:~$ ` |

## Adding a New Command

1. Create `commands/impl/MyCommand.kt` implementing `Command` (add `val description` + `fun execute`)
2. Register it in `CommandRegistry`
3. `-h` support is automatic via `CommandProcessor`

## Android-Specific Notes

- `LauncherActivity` intercepts back press to no-op (launchers must not exit on back)
- `FLAG_ACTIVITY_NEW_TASK` required when starting other apps from a launcher context
- `windowSoftInputMode="adjustResize"` keeps keyboard visible; layout shrinks instead
- `QUERY_ALL_PACKAGES` + `<queries>` in manifest required on Android 11+ for full app list
- Use `commit()` not `apply()` in `FolderManager` — `apply()` async writes caused assignment data loss on restart
