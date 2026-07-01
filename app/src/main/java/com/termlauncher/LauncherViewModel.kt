package com.termlauncher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.termlauncher.apps.AppManager
import com.termlauncher.apps.FavoritesManager
import com.termlauncher.apps.FolderManager
import com.termlauncher.commands.AliasManager
import com.termlauncher.commands.CommandContext
import com.termlauncher.commands.CommandProcessor
import com.termlauncher.commands.SessionState
import com.termlauncher.settings.Settings
import com.termlauncher.settings.SettingsManager
import com.termlauncher.settings.UiMode
import java.util.Calendar
import com.termlauncher.terminal.TerminalEntry
import com.termlauncher.theme.HackerTheme
import com.termlauncher.theme.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val themeManager = ThemeManager(application)
    private val appManager = AppManager(application)
    private val favoritesManager = FavoritesManager(application)
    private val folderManager = FolderManager(application)
    private val aliasManager = AliasManager(application)
    private val session = SessionState()
    private val settingsManager = SettingsManager(application)

    private val _entries = MutableStateFlow<List<TerminalEntry>>(
        listOf(TerminalEntry.info("TermLauncher v1.0 — type 'help' for commands"))
    )
    val entries: StateFlow<List<TerminalEntry>> = _entries.asStateFlow()
    val currentTheme: StateFlow<HackerTheme> = themeManager.currentTheme
    val currentSettings: StateFlow<Settings> = settingsManager.settings

    private val processor = CommandProcessor(
        CommandContext(
            appManager = appManager,
            favoritesManager = favoritesManager,
            folderManager = folderManager,
            aliasManager = aliasManager,
            settingsManager = settingsManager,
            themeManager = themeManager,
            session = session,
            androidContext = application,
            onClear = { _entries.value = emptyList() }
        )
    )

    fun submit(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        val results = processor.process(trimmed)
        val modern = settingsManager.settings.value.uiMode == UiMode.MODERN
        val success = results.none { it.type == TerminalEntry.Type.ERROR }
        val timestamp = if (modern) currentTime() else null
        val inputEntry = TerminalEntry.input(trimmed, timestamp, success)
        val tail = if (modern) listOf(TerminalEntry.divider()) else emptyList()
        _entries.value = _entries.value + listOf(inputEntry) + results + tail
    }

    fun launchApp(packageName: String) {
        val app = appManager.getApps().find { it.packageName == packageName } ?: return
        appManager.launch(app)
        _entries.value = _entries.value + TerminalEntry.info("launching ${app.label}...")
    }

    fun iconFor(packageName: String) = appManager.iconFor(packageName)

    fun isFavorite(packageName: String) = favoritesManager.contains(packageName)

    fun toggleFavorite(packageName: String) {
        val app = appManager.getApps().find { it.packageName == packageName } ?: return
        if (favoritesManager.contains(packageName)) {
            favoritesManager.remove(packageName)
            _entries.value = _entries.value + TerminalEntry.info("removed ${app.label} from favorites")
        } else {
            favoritesManager.add(packageName)
            _entries.value = _entries.value + TerminalEntry.info("added ${app.label} to favorites")
        }
    }

    fun applySettings(panelId: Long, theme: String, uiMode: UiMode, fontSize: Float, prompt: String) {
        themeManager.setTheme(theme)
        settingsManager.set("ui_mode", uiMode.name)
        settingsManager.set("font_size", fontSize.toInt().toString())
        settingsManager.set("prompt", prompt)
        _entries.value = _entries.value.filterNot { it.id == panelId } + TerminalEntry.output("settings saved")
    }

    private fun currentTime(): String {
        val c = Calendar.getInstance()
        return "%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }
}
