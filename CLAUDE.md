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

## Commit Convention & Releases

All commits on `main` must follow [Conventional Commits](https://www.conventionalcommits.org/): `<type>(optional-scope): <description>`, e.g. `feat: add bg_opacity setting` or `fix(alias): handle recursive expansion depth`.

Common types: `feat`, `fix`, `refactor`, `docs`, `chore`, `test`, `style`, `perf`, `build`, `ci`.

`.github/workflows/release.yml` inspects the commit messages pushed to `main` and automatically versions/tags/releases based on the **highest-precedence type found since the last tag**:

| Commit pattern | Version bump |
|---|---|
| `<type>!: ...` (e.g. `feat!:`) or a `BREAKING CHANGE:` footer | major |
| `feat: ...` | minor |
| `fix: ...` or `style: ...` | patch |
| anything else (`chore`, `docs`, `refactor`, `test`, ...) | no release |

When a release-worthy commit lands, the pipeline: bumps `versionName`/`versionCode` in `app/build.gradle.kts` and commits that back to `main` (with `[skip ci]` to avoid retriggering itself) → creates and pushes a `vX.Y.Z` tag on that commit → builds `assembleRelease` (minified, signed with the stable release keystore) → renames the APK to `termlauncher-release-vX_Y_Z.apk` (dots replaced with underscores) → publishes a GitHub Release with that APK attached and an auto-generated changelog from the commit subjects.

Pushes with only non-release-worthy commit types run the workflow but exit early without tagging or releasing anything.

### Release Signing

Release APKs are signed with a **stable keystore** so every release installs as an in-place update — no uninstall, no wiped user data. The signing config in `app/build.gradle.kts` reads the keystore from env vars when `RELEASE_KEYSTORE_FILE` is set (CI), and falls back to the debug key for local `assembleRelease`.

The keystore + credentials live in GitHub Actions repo secrets (never committed — the repo is public):

| Secret | Contents |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | base64 of the release `.keystore` file |
| `RELEASE_KEYSTORE_PASSWORD` | keystore password |
| `RELEASE_KEY_ALIAS` | key alias (`termlauncher`) |
| `RELEASE_KEY_PASSWORD` | key password |

The workflow decodes the keystore to `$RUNNER_TEMP/release.keystore`, exports `RELEASE_KEYSTORE_FILE`, and runs `assembleRelease`. **Never rotate this keystore** — a new key would break in-place updates for everyone. The master copy lives outside the repo on the maintainer's machine (`~/term_launcher-release.keystore`).

## Architecture

### Entry Point & Home Screen Registration

`LauncherActivity` is the single Activity. `AndroidManifest.xml` declares it with two intent filters on the same activity: `CATEGORY_HOME` + `CATEGORY_DEFAULT` (so Android offers it as a launcher choice) and a separate `CATEGORY_LAUNCHER` filter (so it also shows up as a regular app icon in the app drawer of whichever launcher is active). Also declares `QUERY_ALL_PACKAGES` permission and `<queries>` intent filter — required on Android 11+ for `PackageManager` to return all installed apps.

### Terminal UI Model

The screen is split into two parts:
- **Output area** (`RecyclerView`): scrollable list of `TerminalEntry` items. New entries append at the bottom; the list auto-scrolls. `TerminalAdapter` uses three view types depending on the active UI mode.
- **Input bar** (pinned `LinearLayout` at bottom): prompt `TextView` + command `EditText`.

`TerminalEntry` carries: `text`, `type` (INPUT/OUTPUT/ERROR/INFO/DIVIDER), `timestamp` (set in modern mode), `success` (true/false for INPUT entries, drives indicator color), `packageName` (set via `TerminalEntry.app()` on app-listing lines; drives the modern-mode play button).

### UI Modes

Controlled by `settings set ui_mode terminal|modern`. The Activity collects `currentSettings` StateFlow and calls `applySettings()` + `applyInputBarStyle()` live.

**Terminal mode**: plain monospace text, `$` prefix on commands, no decorations.

**Modern mode** (default):
- Input bar gets a rounded rectangle border (10dp corners) in the theme's prompt color
- INPUT entries render with a `◎` indicator (green = `theme.foreground`, red = `theme.error`), command text, and `HH:mm` timestamp right-aligned
- A faint divider line is appended after each command block
- Entries that represent an app (from `apps`, `ls`, `fav list`, `fav add/remove` multi-match, `open` multi-match) render as a bordered card with the app icon and name on the left and three action buttons on the right: `▶` launch, `ⓘ` open system App Info, `★`/`☆` toggle favorite (filled when favorited)
- `settings` (bare/`list`) renders as an interactive panel instead of plain text: theme chips, a terminal/modern toggle, a font-size stepper, and an editable prompt field, with cancel/save buttons at the bottom. Edits are local to the panel (a `SettingsSnapshot` captured at command time) until Save is tapped; Save applies the changes and closes the panel (removed from entries + a "settings saved" line). Cancel closes the panel without saving. Terminal mode keeps the original plain-text listing.
- `alias` (bare/`list`) renders as an interactive panel the same way: existing aliases listed with a `✕` remove button each, plus name/command fields and a `➕` add button to stage new ones (an `AliasSnapshot` captured at command time). Save diffs the staged list against the snapshot (removes dropped names, sets the rest) and closes the panel; Cancel closes without saving. Terminal mode keeps the original plain-text listing.
- `themes` renders as a panel of theme chips (a `ThemeSnapshot` captured at command time); tapping a chip applies that theme immediately via `ThemeManager` — no draft/save step, since there's nothing to stage. The active chip is derived live from the adapter's current `theme`, so it re-highlights correctly on every rebind. Terminal mode keeps the original plain-text listing. Theme chips (here and in the settings panel) wrap into fixed rows of `THEME_CHIPS_PER_ROW` (3) via `TerminalAdapter.buildThemeChips()` — deliberately not a scrollable single row, so all themes stay visible without a hidden scroll affordance.
- `ls` at root tags each folder line with `folderName` (`TerminalEntry.folder()`), rendering a line-based outline folder icon (`ic_folder_outline`, tinted to `theme.foreground`) + name (brackets stripped) instead of plain `[name]` text. Terminal mode keeps the original plain-text listing.

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

`AppManager` queries `PackageManager` for all apps with `CATEGORY_LAUNCHER`. Caches the list (label, package name, launch intent, icon `Drawable`) and refreshes on package add/remove/replace broadcasts. `open` checks favorites first, then all apps.

### Persistence

All user data is stored in SharedPreferences and survives app restarts:

| Store | File | Contents |
|---|---|---|
| `ThemeManager` | `"theme"` | Active theme name |
| `SettingsManager` | `"app_settings"` | font_size, prompt, ui_mode, bg_opacity |
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
| `theme/HackerTheme.kt` | Theme data class + predefined themes: matrix, blood, ice, amber, ghost, ubuntu, dracula, frappe |
| `terminal/TerminalAdapter.kt` | RecyclerView adapter; 3 view types: text, modern-input, divider |
| `terminal/TerminalEntry.kt` | Data class: text, type, timestamp, success |

## Built-in Commands

Every command supports `-h` for inline help (handled globally in `CommandProcessor`).

| Command | Description |
|---|---|
| `open <name>` / `launch <name>` | Fuzzy-match and launch an app; checks favorites first |
| `apps [-f <filter>]` | List all installed apps; `-f` filters by name |
| `find <name>` | Search installed apps by name (same as `apps -f <name>`) |
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
| `theme` | matrix, blood, ice, amber, ghost, ubuntu, dracula, frappe | matrix |
| `ui_mode` | terminal, modern | modern |
| `font_size` | 8–32 (sp) | 14 |
| `prompt` | any string | `root@hackr:~$ ` |
| `bg_opacity` | 0–100 (%) | 100 |

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
- Home-screen wallpaper visibility (`bg_opacity` setting): `Theme.TermLauncher` sets `android:windowBackground` to transparent and `android:windowShowWallpaper` to true, and `LauncherActivity.onCreate()` adds `WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER`. `rootLayout`'s background color alpha channel (and the modern input bar's fill) are derived from `theme.background` + `settings.bgOpacity` via `colorWithOpacity()` — at 100% it looks identical to before this existed
