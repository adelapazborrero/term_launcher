package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class ThemesCommand : Command {
    override val description = "themes  — list available color themes"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
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
