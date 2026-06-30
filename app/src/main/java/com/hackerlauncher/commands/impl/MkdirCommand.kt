package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class MkdirCommand : Command {
    override val description = "mkdir <name>  — create an app folder"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val name = args.joinToString(" ").trimEnd('/').trim().lowercase()
        if (name.isBlank()) return listOf(TerminalEntry.error("usage: mkdir <folder name>"))
        if (name == "apps") return listOf(TerminalEntry.error("'apps' is a reserved folder name"))
        return if (context.folderManager.createFolder(name)) {
            listOf(TerminalEntry.output("folder [$name] created"))
        } else {
            listOf(TerminalEntry.error("folder '$name' already exists"))
        }
    }
}
