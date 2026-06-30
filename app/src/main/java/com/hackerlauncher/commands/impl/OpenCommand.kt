package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class OpenCommand : Command {
    override val description = "open <app>  — launch an installed app by name (partial match ok)"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.isEmpty()) return listOf(TerminalEntry.error("usage: open <app name>"))

        val query = args.joinToString(" ")
        val matches = context.appManager.findApps(query)

        return when {
            matches.isEmpty() -> listOf(
                TerminalEntry.error("no app found matching '$query'"),
                TerminalEntry.info("hint: 'apps $query' to search the app list")
            )
            matches.size == 1 -> {
                context.appManager.launch(matches.first())
                listOf(TerminalEntry.info("launching ${matches.first().label}..."))
            }
            else -> buildList {
                add(TerminalEntry.info("multiple matches for '$query':"))
                matches.take(10).forEach { add(TerminalEntry.output("  ${it.label}")) }
                if (matches.size > 10) add(TerminalEntry.info("  ...and ${matches.size - 10} more"))
                add(TerminalEntry.info("be more specific to launch"))
            }
        }
    }
}
