package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class MvCommand : Command {
    override val description = "mv <app> <folder|..>  — move app into folder; .. removes it from all folders"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.size < 2) return listOf(TerminalEntry.error("usage: mv <app name> <folder|..>"))

        val dest = args.last().trimEnd('/').lowercase()
        val query = args.dropLast(1).joinToString(" ")
        val matches = context.appManager.findApps(query)

        return when {
            matches.isEmpty() -> listOf(TerminalEntry.error("no app found matching '$query'"))
            matches.size > 1 -> buildList {
                add(TerminalEntry.info("multiple matches — be more specific:"))
                matches.take(8).forEach { add(TerminalEntry.output("  ${it.label}")) }
            }
            dest == ".." -> {
                context.folderManager.removeAppFromAllFolders(matches.first().packageName)
                listOf(TerminalEntry.output("${matches.first().label} removed from all folders"))
            }
            context.folderManager.folderExists(dest) -> {
                context.folderManager.addApp(matches.first().packageName, dest)
                listOf(TerminalEntry.output("${matches.first().label} → [$dest]"))
            }
            else -> listOf(TerminalEntry.error("folder '$dest' not found — use 'ls' or 'mkdir $dest'"))
        }
    }
}
