package com.termlauncher.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {

    private val prefs = context.getSharedPreferences("theme", Context.MODE_PRIVATE)
    private val _currentTheme = MutableStateFlow(
        Themes.ALL.find { it.name == prefs.getString("theme", "matrix") } ?: Themes.MATRIX
    )
    val currentTheme: StateFlow<HackerTheme> = _currentTheme.asStateFlow()

    fun setTheme(name: String): Boolean {
        val theme = Themes.ALL.find { it.name.equals(name, ignoreCase = true) } ?: return false
        _currentTheme.value = theme
        prefs.edit().putString("theme", theme.name).apply()
        return true
    }

    fun getAvailableThemes(): List<String> = Themes.ALL.map { it.name }
}
