package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class AppsCommand : Command {
    override val description = "apps [filter]  — list installed apps, optional filter by name"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val filter = args.firstOrNull()
        val apps = if (filter != null) context.appManager.findApps(filter)
                   else context.appManager.getApps()

        if (apps.isEmpty()) return listOf(TerminalEntry.info("no apps found"))

        return buildList {
            add(TerminalEntry.info("${apps.size} app(s)${if (filter != null) " matching '$filter'" else ""}:"))
            apps.forEach { add(TerminalEntry.output("  ${it.label}")) }
        }
    }
}
