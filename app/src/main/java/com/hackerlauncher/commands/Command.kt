package com.hackerlauncher.commands

import com.hackerlauncher.terminal.TerminalEntry

interface Command {
    val description: String
    fun execute(args: List<String>, context: CommandContext): List<TerminalEntry>
}
