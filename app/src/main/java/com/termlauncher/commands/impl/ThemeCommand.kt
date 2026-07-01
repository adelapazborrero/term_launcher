package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.terminal.TerminalEntry

class ThemeCommand : Command {
    override val description = "theme <name>  — switch color theme"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.isEmpty()) {
            val current = context.themeManager.currentTheme.value.name
            return listOf(
                TerminalEntry.info("current theme: $current"),
                TerminalEntry.info("usage: theme <name>  |  type 'themes' to list all")
            )
        }
        val name = args.first()
        return if (context.themeManager.setTheme(name)) {
            listOf(TerminalEntry.info("theme changed to '$name'"))
        } else {
            listOf(
                TerminalEntry.error("unknown theme: '$name'"),
                TerminalEntry.info("available: ${context.themeManager.getAvailableThemes().joinToString(", ")}")
            )
        }
    }
}
