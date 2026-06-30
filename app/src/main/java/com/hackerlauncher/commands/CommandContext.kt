package com.hackerlauncher.commands

import android.content.Context
import com.hackerlauncher.apps.AppManager
import com.hackerlauncher.apps.FavoritesManager
import com.hackerlauncher.apps.FolderManager
import com.hackerlauncher.settings.SettingsManager
import com.hackerlauncher.theme.ThemeManager

data class CommandContext(
    val appManager: AppManager,
    val favoritesManager: FavoritesManager,
    val folderManager: FolderManager,
    val aliasManager: AliasManager,
    val settingsManager: SettingsManager,
    val themeManager: ThemeManager,
    val session: SessionState,
    val androidContext: Context,
    val onClear: () -> Unit
)
