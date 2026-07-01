package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.terminal.TerminalEntry

class FindCommand : Command {
    override val description = "find <name>  — search installed apps by name (same as 'apps -f <name>')"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> =
        AppsListCommand().execute(listOf("-f") + args, context)
}
