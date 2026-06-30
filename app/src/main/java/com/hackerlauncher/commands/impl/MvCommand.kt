package com.hackerlauncher.commands.impl

import com.hackerlauncher.apps.AppInfo
import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class MvCommand : Command {
    override val description = "mv <source> <dest>  — move app to folder; supports paths like apps/Spotify music/"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.size < 2) return listOf(TerminalEntry.error("usage: mv <app|folder/app> <folder|.|..>"))

        val rawDest = args.last().trimEnd('/')
        val rawSource = args.dropLast(1).joinToString(" ")

        val dest = resolveDest(rawDest, context)
            ?: return listOf(TerminalEntry.error(
                if (rawDest == ".") "not in a folder — cd into one first, or use a folder name"
                else "folder '$rawDest' not found — use 'ls' or 'mkdir $rawDest'"
            ))

        val (matches, sourceFolder) = resolveSource(rawSource, context)

        return when {
            matches.isEmpty() -> listOf(TerminalEntry.error("no app found matching '$rawSource'"))
            matches.size > 1 -> buildList {
                add(TerminalEntry.info("multiple matches — be more specific:"))
                matches.take(8).forEach { add(TerminalEntry.output("  • ${it.label}")) }
            }
            else -> {
                val app = matches.first()
                when (dest) {
                    REMOVE -> {
                        context.folderManager.removeAppFromAllFolders(app.packageName)
                        listOf(TerminalEntry.output("${app.label} removed from all folders"))
                    }
                    else -> {
                        if (!context.folderManager.folderExists(dest))
                            return listOf(TerminalEntry.error("folder '$dest' not found"))
                        context.folderManager.addApp(app.packageName, dest)
                        listOf(TerminalEntry.output("${app.label} → [$dest]"))
                    }
                }
            }
        }
    }

    private fun resolveDest(raw: String, context: CommandContext): String? {
        return when (val d = raw.lowercase()) {
            "." -> context.session.currentFolder  // null if at root → caller returns error
            ".." -> REMOVE
            "apps" -> REMOVE  // move to uncategorized
            else -> if (context.folderManager.folderExists(d)) d else null
        }
    }

    private fun resolveSource(raw: String, context: CommandContext): Pair<List<AppInfo>, String?> {
        val slashIdx = raw.lastIndexOf('/')
        if (slashIdx < 0) {
            // No path prefix — search in current folder context
            return searchInFolder(raw, context.session.currentFolder, context) to context.session.currentFolder
        }

        val pathPart = raw.substring(0, slashIdx)
        val appQuery = raw.substring(slashIdx + 1)
        val folder = resolveFolderPath(pathPart, context.session.currentFolder)
        return searchInFolder(appQuery, folder, context) to folder
    }

    private fun resolveFolderPath(path: String, current: String?): String? {
        var ctx: String? = current
        for (segment in path.split("/").filter { it.isNotEmpty() }) {
            ctx = when (segment) {
                ".." -> null
                "." -> ctx
                "apps" -> "apps"
                else -> segment
            }
        }
        return ctx
    }

    private fun searchInFolder(query: String, folder: String?, context: CommandContext): List<AppInfo> {
        val allApps = context.appManager.getApps()
        val pool = when (folder) {
            null, "apps" -> {
                val folderedPkgs = context.folderManager.getFolders()
                    .flatMap { context.folderManager.getAppsInFolder(it) }.toSet()
                allApps.filter { it.packageName !in folderedPkgs }
            }
            else -> {
                val pkgs = context.folderManager.getAppsInFolder(folder)
                allApps.filter { it.packageName in pkgs }
            }
        }
        val lower = query.lowercase()
        return pool.filter { it.label.lowercase().contains(lower) }
            .sortedBy {
                val lbl = it.label.lowercase()
                when { lbl == lower -> 0; lbl.startsWith(lower) -> 1; else -> 2 }
            }
    }

    companion object {
        private const val REMOVE = "__remove__"
    }
}
