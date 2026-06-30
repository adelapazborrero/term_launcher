package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class CdCommand : Command {
    override val description = "cd [folder|..]  — enter a folder or go back to root"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val target = args.joinToString(" ").trimEnd('/').trim().lowercase()
        return when {
            target.isEmpty() || target == ".." -> {
                context.session.currentFolder = null
                listOf(TerminalEntry.output("/"))
            }
            target == "apps" -> {
                context.session.currentFolder = "apps"
                listOf(TerminalEntry.output("/apps"))
            }
            context.folderManager.folderExists(target) -> {
                context.session.currentFolder = target
                listOf(TerminalEntry.output("/$target"))
            }
            else -> listOf(TerminalEntry.error("folder '$target' not found — use 'ls' to see folders"))
        }
    }
}
