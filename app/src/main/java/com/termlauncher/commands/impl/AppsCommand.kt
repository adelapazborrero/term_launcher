package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.terminal.TerminalEntry

class AppsCommand : Command {
    override val description = "ls [folder]  — list folders; ls apps for uncategorized; ls <folder> for contents"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val arg = args.firstOrNull()?.trimEnd('/')?.lowercase()
        val current = context.session.currentFolder
        return when {
            arg == "apps" -> listUncategorized(context)
            arg != null && context.folderManager.folderExists(arg) -> listFolder(arg, context)
            arg != null -> listOf(TerminalEntry.error("folder '$arg' not found — use 'ls' to see folders"))
            current == "apps" -> listUncategorized(context)
            current != null -> listFolder(current, context)
            else -> listRoot(context)
        }
    }

    private fun listRoot(context: CommandContext): List<TerminalEntry> = buildList {
        val folders = context.folderManager.getFolders()
        add(TerminalEntry.info("folders:"))
        add(TerminalEntry.output("  [apps]"))
        folders.forEach { add(TerminalEntry.output("  [$it]")) }
        add(TerminalEntry.info("use 'ls <folder>' or 'cd <folder>' to explore"))
    }

    private fun listUncategorized(context: CommandContext): List<TerminalEntry> = buildList {
        val folderedPackages = context.folderManager.getFolders()
            .flatMap { context.folderManager.getAppsInFolder(it) }.toSet()
        val apps = context.appManager.getApps().filter { it.packageName !in folderedPackages }
        if (apps.isEmpty()) {
            add(TerminalEntry.info("no uncategorized apps"))
        } else {
            add(TerminalEntry.info("[apps] — ${apps.size} app(s):"))
            apps.forEach { add(TerminalEntry.output("  ${it.label}")) }
        }
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
}
