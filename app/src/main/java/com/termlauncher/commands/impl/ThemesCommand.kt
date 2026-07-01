package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.settings.UiMode
import com.termlauncher.terminal.TerminalEntry
import com.termlauncher.terminal.ThemeSnapshot

class ThemesCommand : Command {
    override val description = "themes  — list available color themes"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (context.settingsManager.getAll().uiMode == UiMode.MODERN) {
            return listOf(TerminalEntry.themePanel(ThemeSnapshot(context.themeManager.getAvailableThemes())))
        }
        val current = context.themeManager.currentTheme.value.name
        return buildList {
            add(TerminalEntry.info("available themes (* = active):"))
            context.themeManager.getAvailableThemes().forEach { name ->
                val marker = if (name == current) " *" else ""
                add(TerminalEntry.output("  $name$marker"))
            }
        }
    }
}
