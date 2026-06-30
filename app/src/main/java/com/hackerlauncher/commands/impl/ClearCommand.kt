package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class ClearCommand : Command {
    override val description = "clear  — clear terminal output"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        context.onClear()
        return emptyList()
    }
}
