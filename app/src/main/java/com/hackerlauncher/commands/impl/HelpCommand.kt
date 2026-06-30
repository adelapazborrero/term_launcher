package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.commands.CommandRegistry
import com.hackerlauncher.terminal.TerminalEntry

class HelpCommand : Command {
    override val description = "help  — list available commands"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        return buildList {
            add(TerminalEntry.info("HackerLauncher — commands:"))
            add(TerminalEntry.output(""))
            CommandRegistry.getUniqueDescriptions().forEach { (_, desc) ->
                add(TerminalEntry.output("  $desc"))
            }
            add(TerminalEntry.output(""))
            add(TerminalEntry.info("aliases: launch/start=open, ls=apps, cls=clear, ?=help"))
        }
    }
}
