# HackerLauncher

A home screen replacement for Android with a terminal aesthetic. No icons, no widgets — just a command prompt. Every interaction happens through typed commands, giving your phone the feel of a hacker's workstation.

## Screenshots

*Coming soon*

---

## Features

- **Pure terminal interface** — input bar always pinned at the bottom, output scrolls up like a real terminal session
- **Two UI modes** — classic full-terminal or a modernized look with status indicators, timestamps, and a clean input border
- **App folders** — organize apps into named folders, navigate them with `ls`, `cd`, and `mv` just like a filesystem
- **Favorites** — pin your most-used apps so `open` finds them first
- **Aliases** — map short commands to longer ones (`alias br "open brave"`)
- **Persistent settings** — theme, font size, prompt string, and UI mode all survive restarts
- **5 built-in themes** — matrix, blood, ice, amber, ghost
- **Fully customizable prompt** — change the prompt string to anything you like

---

## Installation

HackerLauncher is a debug/sideload build — not yet on the Play Store.

1. Download the latest `app-debug.apk` from the [Releases](#) page
2. On your Android device, enable **Install unknown apps** for your file manager
3. Open the APK and tap **Install**
4. Press the **Home button** — Android will ask which launcher to use; choose **HackerLauncher**

> Requires Android 11 or later.

---

## Getting Started

When you first open HackerLauncher, type `help` to see available commands. Try a few:

```
help              — list all commands
apps              — show all installed apps
open spotify      — launch Spotify (partial name match works)
themes            — list available themes
settings set theme blood
settings set ui_mode modern
```

---

## Command Reference

Every command supports `-h` for quick usage info (e.g. `mv -h`).

### Apps & Launching

| Command | Description |
|---|---|
| `open <name>` | Launch an app by name — partial match works |
| `apps` | List all installed apps |
| `apps -f <query>` | Filter the app list by name |

### Folders

Organize apps into named folders, just like directories.

| Command | Description |
|---|---|
| `ls` | Show all folders (including the built-in `[apps]` folder) |
| `ls <folder>` | Show apps inside a folder |
| `ls apps` | Show all uncategorized apps |
| `cd <folder>` | Navigate into a folder |
| `cd ..` | Return to root |
| `mkdir <name>` | Create a new folder |
| `mv <app> <folder>/` | Move an app into a folder |
| `mv <app> ..` | Remove an app from its folder |
| `mv apps/Spotify music/` | Move using folder paths |

### Favorites

Favorites are checked first when using `open`, so your most-used apps launch faster.

| Command | Description |
|---|---|
| `fav add <name>` | Add an app to favorites |
| `fav remove <name>` | Remove from favorites |
| `fav list` | Show all favorites |

### Aliases

```
alias br "open brave"     — now typing 'br' launches Brave
alias list                — show all aliases
alias remove br           — remove an alias
```

Quotes are required when the expansion contains spaces.

### Settings

```
settings list                        — show all current settings
settings set theme <name>            — switch theme
settings set ui_mode terminal        — classic terminal look
settings set ui_mode modern          — modern look with indicators + timestamps
settings set font_size 16            — text size in sp (8–32)
settings set prompt "hack@r:~$ "     — custom prompt string
settings reset                       — restore all defaults
```

### Themes

| Name | Look |
|---|---|
| `matrix` | Black background, green text |
| `blood` | Dark red background, bright red text |
| `ice` | Dark blue background, light blue text |
| `amber` | Dark background, amber/orange text |
| `ghost` | Near-black background, white/grey text |

### Other

| Command | Description |
|---|---|
| `help` | Bullet list of all commands |
| `help -l` | Verbose help with descriptions |
| `themes` | List available themes |
| `info` | Device info (model, Android version, battery, time) |
| `clear` | Clear the terminal output |

---

## UI Modes

**Terminal mode** (default) — plain monospace text, `$` prefix on commands, no decorations. Pure and minimal.

**Modern mode** (`settings set ui_mode modern`) — the same command-driven interface with a few visual upgrades:
- Rounded border on the input bar
- A thin separator line above the input area
- `◎` status indicator per command: theme color on success, error color on failure
- Timestamp (`HH:mm`) shown on the right of each command
- Faint divider between command blocks

All colors follow the active theme — switching themes updates everything including the indicators.

---

## Building from Source

```bash
git clone https://github.com/yourname/HackerLauncher
cd HackerLauncher
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

Requires Android Studio or the Android SDK with `ANDROID_HOME` set. Targets Android 11+ (API 30).

---

## License

MIT
