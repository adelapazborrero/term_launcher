package com.termlauncher.commands

import com.termlauncher.terminal.TerminalEntry

interface Command {
    val description: String
    fun execute(args: List<String>, context: CommandContext): List<TerminalEntry>
}
