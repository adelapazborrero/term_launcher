package com.hackerlauncher.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager {
    private val _currentTheme = MutableStateFlow(Themes.MATRIX)
    val currentTheme: StateFlow<HackerTheme> = _currentTheme.asStateFlow()

    fun setTheme(name: String): Boolean {
        val theme = Themes.ALL.find { it.name.equals(name, ignoreCase = true) }
            ?: return false
        _currentTheme.value = theme
        return true
    }

    fun getAvailableThemes(): List<String> = Themes.ALL.map { it.name }
}
