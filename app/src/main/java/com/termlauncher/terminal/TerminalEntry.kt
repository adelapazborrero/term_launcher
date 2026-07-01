package com.termlauncher.terminal

import com.termlauncher.settings.UiMode
import java.util.concurrent.atomic.AtomicLong

data class TerminalEntry(
    val text: String,
    val type: Type,
    val timestamp: String? = null,
    val success: Boolean = true,
    val packageName: String? = null,
    val settingsSnapshot: SettingsSnapshot? = null,
    val id: Long = nextId()
) {
    enum class Type { INPUT, OUTPUT, ERROR, INFO, DIVIDER, SETTINGS_PANEL }

    companion object {
        private val idCounter = AtomicLong(0)
        private fun nextId() = idCounter.incrementAndGet()

        fun output(text: String) = TerminalEntry(text, Type.OUTPUT)
        fun error(text: String) = TerminalEntry(text, Type.ERROR, success = false)
        fun info(text: String) = TerminalEntry(text, Type.INFO)
        fun input(text: String, timestamp: String? = null, success: Boolean = true) =
            TerminalEntry(text, Type.INPUT, timestamp, success)
        fun divider() = TerminalEntry("", Type.DIVIDER)
        fun app(text: String, packageName: String) = TerminalEntry(text, Type.OUTPUT, packageName = packageName)
        fun settingsPanel(snapshot: SettingsSnapshot) =
            TerminalEntry("⚙ settings", Type.SETTINGS_PANEL, settingsSnapshot = snapshot)
    }
}

data class SettingsSnapshot(
    val theme: String,
    val availableThemes: List<String>,
    val uiMode: UiMode,
    val fontSize: Float,
    val prompt: String
)
