package com.termlauncher.commands

import android.content.Context
import com.termlauncher.apps.AppManager
import com.termlauncher.apps.FavoritesManager
import com.termlauncher.apps.FolderManager
import com.termlauncher.settings.SettingsManager
import com.termlauncher.theme.ThemeManager

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
