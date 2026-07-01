package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.terminal.TerminalEntry

class AppsListCommand : Command {
    override val description = "apps [-f <filter>]  — list all installed apps, optional name filter"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val filterIdx = args.indexOf("-f")
        val query = if (filterIdx >= 0 && args.size > filterIdx + 1) args[filterIdx + 1] else null
        val apps = if (query != null) context.appManager.findApps(query)
                   else context.appManager.getApps()
        return buildList {
            if (apps.isEmpty()) { add(TerminalEntry.info("no apps found")); return@buildList }
            add(TerminalEntry.info("${apps.size} app(s)${if (query != null) " matching '$query'" else ""}:"))
            apps.forEach { add(TerminalEntry.output("  • ${it.label}")) }
        }
    }
}
