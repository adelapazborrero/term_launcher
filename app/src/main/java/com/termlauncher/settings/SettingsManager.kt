package com.termlauncher.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    fun getAll(): Settings = _settings.value

    fun set(key: String, value: String): Boolean {
        val current = _settings.value
        val updated = when (key) {
            "font_size" -> {
                val sp = value.toFloatOrNull()?.takeIf { it in 8f..32f } ?: return false
                current.copy(fontSize = sp)
            }
            "prompt" -> current.copy(prompt = value)
            "ui_mode" -> {
                val mode = UiMode.entries.find { it.name.equals(value, ignoreCase = true) }
                    ?: return false
                current.copy(uiMode = mode)
            }
            "bg_opacity" -> {
                val pct = value.toIntOrNull()?.takeIf { it in 0..100 } ?: return false
                current.copy(bgOpacity = pct)
            }
            else -> return false
        }
        save(updated)
        _settings.value = updated
        return true
    }

    fun reset() {
        val defaults = Settings()
        save(defaults)
        _settings.value = defaults
    }

    private fun save(s: Settings) {
        prefs.edit()
            .putFloat("font_size", s.fontSize)
            .putString("prompt", s.prompt)
            .putString("ui_mode", s.uiMode.name)
            .putInt("bg_opacity", s.bgOpacity)
            .apply()
    }

    private fun load() = Settings(
        fontSize = prefs.getFloat("font_size", 14f),
        prompt = prefs.getString("prompt", "root@hackr:~$ ") ?: "root@hackr:~$ ",
        uiMode = prefs.getString("ui_mode", "MODERN")
            ?.let { name -> UiMode.entries.find { it.name == name } } ?: UiMode.MODERN,
        bgOpacity = prefs.getInt("bg_opacity", 100)
    )
}
