package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.commands.CommandRegistry
import com.hackerlauncher.terminal.TerminalEntry

class HelpCommand : Command {
    override val description = "help [-l] [cmd]  — list commands; -l for details"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val flag = args.firstOrNull()
        return when {
            flag == "-l" -> verbose()
            flag != null -> specific(flag)
            else -> short()
        }
    }

    private fun short(): List<TerminalEntry> {
        val names = CommandRegistry.getUniqueNames().joinToString("  ")
        return listOf(
            TerminalEntry.info("commands: $names"),
            TerminalEntry.info("help -l for details  |  help <cmd> for usage")
        )
    }

    private fun verbose(): List<TerminalEntry> = buildList {
        add(TerminalEntry.info("HackerLauncher — commands:"))
        CommandRegistry.getUniqueDescriptions().forEach { (_, desc) ->
            add(TerminalEntry.output("  $desc"))
        }
    }

    private fun specific(name: String): List<TerminalEntry> {
        val desc = CommandRegistry.getUniqueDescriptions()
            .find { it.first.equals(name, ignoreCase = true) }?.second
            ?: return listOf(TerminalEntry.error("unknown command: $name"))
        return listOf(TerminalEntry.output(desc))
    }
}
