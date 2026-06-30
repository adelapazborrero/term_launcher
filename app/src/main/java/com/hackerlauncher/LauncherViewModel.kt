package com.hackerlauncher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hackerlauncher.apps.AppManager
import com.hackerlauncher.apps.FavoritesManager
import com.hackerlauncher.commands.AliasManager
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.commands.CommandProcessor
import com.hackerlauncher.settings.Settings
import com.hackerlauncher.settings.SettingsManager
import com.hackerlauncher.terminal.TerminalEntry
import com.hackerlauncher.theme.HackerTheme
import com.hackerlauncher.theme.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val themeManager = ThemeManager(application)
    private val appManager = AppManager(application)
    private val favoritesManager = FavoritesManager(application)
    private val aliasManager = AliasManager(application)
    private val settingsManager = SettingsManager(application)

    private val _entries = MutableStateFlow<List<TerminalEntry>>(
        listOf(TerminalEntry.info("HackerLauncher v1.0 — type 'help' for commands"))
    )
    val entries: StateFlow<List<TerminalEntry>> = _entries.asStateFlow()
    val currentTheme: StateFlow<HackerTheme> = themeManager.currentTheme
    val currentSettings: StateFlow<Settings> = settingsManager.settings

    private val processor = CommandProcessor(
        CommandContext(
            appManager = appManager,
            favoritesManager = favoritesManager,
            aliasManager = aliasManager,
            settingsManager = settingsManager,
            themeManager = themeManager,
            androidContext = application,
            onClear = { _entries.value = emptyList() }
        )
    )

    fun submit(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        val results = processor.process(trimmed)
        _entries.value = _entries.value + listOf(TerminalEntry.input("$ $trimmed")) + results
    }
}
