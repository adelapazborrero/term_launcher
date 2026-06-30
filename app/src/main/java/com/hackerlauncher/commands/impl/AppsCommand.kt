package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class AppsCommand : Command {
    override val description = "ls [folder|-a]  — list folders/apps; -a to list all apps"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val arg = args.firstOrNull()
        return when {
            arg == "-a" -> listAll(context)
            arg != null && context.folderManager.folderExists(arg) -> listFolder(arg, context)
            arg != null -> listOf(TerminalEntry.error("folder '$arg' not found — use 'ls' to see folders"))
            context.session.currentFolder != null -> listFolder(context.session.currentFolder!!, context)
            else -> listRoot(context)
        }
    }

    private fun listRoot(context: CommandContext): List<TerminalEntry> = buildList {
        val folders = context.folderManager.getFolders()
        val allApps = context.appManager.getApps()
        val folderedPackages = folders.flatMap { context.folderManager.getAppsInFolder(it) }.toSet()
        val uncategorized = allApps.filter { it.packageName !in folderedPackages }

        if (folders.isNotEmpty()) {
            add(TerminalEntry.info("${folders.size} folder(s):"))
            folders.forEach { add(TerminalEntry.output("  [$it]")) }
        }
        if (uncategorized.isNotEmpty()) {
            add(TerminalEntry.info("${uncategorized.size} app(s):"))
            uncategorized.forEach { add(TerminalEntry.output("  ${it.label}")) }
        }
        if (folders.isEmpty() && uncategorized.isEmpty()) add(TerminalEntry.info("no apps found"))
        add(TerminalEntry.info("use 'ls -a' to see all apps, 'cd <folder>' to enter a folder"))
    }

    private fun listFolder(folder: String, context: CommandContext): List<TerminalEntry> = buildList {
        val packageNames = context.folderManager.getAppsInFolder(folder)
        val apps = context.appManager.getApps().filter { it.packageName in packageNames }
        if (apps.isEmpty()) {
            add(TerminalEntry.info("[$folder] is empty — use 'mv <app> $folder' to add apps"))
        } else {
            add(TerminalEntry.info("[$folder] — ${apps.size} app(s):"))
            apps.sortedBy { it.label.lowercase() }.forEach { add(TerminalEntry.output("  ${it.label}")) }
        }
    }

    private fun listAll(context: CommandContext): List<TerminalEntry> = buildList {
        val apps = context.appManager.getApps()
        if (apps.isEmpty()) { add(TerminalEntry.info("no apps found")); return@buildList }
        add(TerminalEntry.info("${apps.size} app(s):"))
        apps.forEach { add(TerminalEntry.output("  ${it.label}")) }
    }
}
