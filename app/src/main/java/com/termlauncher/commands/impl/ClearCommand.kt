package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.terminal.TerminalEntry

class ClearCommand : Command {
    override val description = "clear  — clear terminal output"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        context.onClear()
        return emptyList()
    }
}
