package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.settings.UiMode
import com.termlauncher.terminal.SettingsSnapshot
import com.termlauncher.terminal.TerminalEntry

class SettingsCommand : Command {
    override val description = "settings <list|set|reset> [key] [value]  — manage app settings"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        return when (args.firstOrNull()?.lowercase()) {
            "list", null -> if (context.settingsManager.getAll().uiMode == UiMode.MODERN) {
                listOf(buildPanel(context))
            } else {
                listSettings(context)
            }
            "set" -> setKey(args.drop(1), context)
            "reset" -> {
                context.settingsManager.reset()
                listOf(TerminalEntry.output("settings reset to defaults"))
            }
            else -> listOf(TerminalEntry.error("usage: settings <list|set|reset> [key] [value]"))
        }
    }

    private fun buildPanel(context: CommandContext): TerminalEntry {
        val s = context.settingsManager.getAll()
        return TerminalEntry.settingsPanel(
            SettingsSnapshot(
                theme = context.themeManager.currentTheme.value.name,
                availableThemes = context.themeManager.getAvailableThemes(),
                uiMode = s.uiMode,
                fontSize = s.fontSize,
                prompt = s.prompt,
                bgOpacity = s.bgOpacity
            )
        )
    }

    private fun listSettings(context: CommandContext): List<TerminalEntry> {
        val s = context.settingsManager.getAll()
        val theme = context.themeManager.currentTheme.value.name
        return buildList {
            add(TerminalEntry.info("current settings:"))
            add(TerminalEntry.output("  theme       = $theme"))
            add(TerminalEntry.output("  ui_mode     = ${s.uiMode.name.lowercase()}"))
            add(TerminalEntry.output("  font_size   = ${s.fontSize.toInt()}sp"))
            add(TerminalEntry.output("  prompt      = ${s.prompt}"))
            add(TerminalEntry.output("  bg_opacity  = ${s.bgOpacity}%"))
            add(TerminalEntry.info("use 'settings set <key> <value>' to change"))
            add(TerminalEntry.info("keys: theme, ui_mode (terminal|modern), font_size (8-32), prompt, bg_opacity (0-100)"))
        }
    }

    private fun setKey(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.size < 2) return listOf(TerminalEntry.error("usage: settings set <key> <value>"))
        val key = args[0].lowercase()
        val value = args.drop(1).joinToString(" ")
        return when (key) {
            "theme" -> {
                if (context.themeManager.setTheme(value)) {
                    listOf(TerminalEntry.output("theme set to: $value"))
                } else {
                    val available = context.themeManager.getAvailableThemes().joinToString(", ")
                    listOf(TerminalEntry.error("unknown theme '$value' — available: $available"))
                }
            }
            else -> if (context.settingsManager.set(key, value)) {
                listOf(TerminalEntry.output("$key set to: $value"))
            } else {
                when (key) {
                    "font_size" -> listOf(TerminalEntry.error("font_size must be a number between 8 and 32"))
                    "ui_mode" -> listOf(TerminalEntry.error("ui_mode must be 'terminal' or 'modern'"))
                    "bg_opacity" -> listOf(TerminalEntry.error("bg_opacity must be a number between 0 and 100"))
                    else -> listOf(TerminalEntry.error("unknown key '$key' — valid keys: theme, ui_mode, font_size, prompt, bg_opacity"))
                }
            }
        }
    }
}
