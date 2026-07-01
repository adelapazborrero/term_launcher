package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.terminal.TerminalEntry

class FavCommand : Command {
    override val description = "fav <add|remove|list> [app]  — manage favorite apps"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val sub = args.firstOrNull()?.lowercase()
        return when (sub) {
            "add" -> addFav(args.drop(1), context)
            "remove", "rm", "del" -> removeFav(args.drop(1), context)
            "list" -> listFavs(context)
            else -> listOf(TerminalEntry.error("usage: fav <add|remove|list> [app name]"))
        }
    }

    private fun addFav(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.isEmpty()) return listOf(TerminalEntry.error("usage: fav add <app name>"))
        val query = args.joinToString(" ")
        val matches = context.appManager.findApps(query)
        return when {
            matches.isEmpty() -> listOf(TerminalEntry.error("no app found matching '$query'"))
            matches.size == 1 -> {
                val app = matches.first()
                if (context.favoritesManager.contains(app.packageName)) {
                    listOf(TerminalEntry.info("${app.label} is already in favorites"))
                } else {
                    context.favoritesManager.add(app.packageName)
                    listOf(TerminalEntry.output("added ${app.label} to favorites"))
                }
            }
            else -> buildList {
                add(TerminalEntry.info("multiple matches — be more specific:"))
                matches.take(8).forEach { add(TerminalEntry.output("  ${it.label}")) }
            }
        }
    }

    private fun removeFav(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.isEmpty()) return listOf(TerminalEntry.error("usage: fav remove <app name>"))
        val query = args.joinToString(" ")
        val favApps = getFavApps(context)
        val matches = favApps.filter { it.label.lowercase().contains(query.lowercase()) }
        return when {
            matches.isEmpty() -> listOf(TerminalEntry.error("'$query' not found in favorites"))
            matches.size == 1 -> {
                context.favoritesManager.remove(matches.first().packageName)
                listOf(TerminalEntry.output("removed ${matches.first().label} from favorites"))
            }
            else -> buildList {
                add(TerminalEntry.info("multiple matches — be more specific:"))
                matches.forEach { add(TerminalEntry.output("  ${it.label}")) }
            }
        }
    }

    private fun listFavs(context: CommandContext): List<TerminalEntry> {
        val favApps = getFavApps(context)
        if (favApps.isEmpty()) return listOf(TerminalEntry.info("no favorites yet — use 'fav add <app>'"))
        return buildList {
            add(TerminalEntry.info("${favApps.size} favorite(s):"))
            favApps.sortedBy { it.label.lowercase() }
                .forEach { add(TerminalEntry.output("  ${it.label}")) }
        }
    }

    private fun getFavApps(context: CommandContext) =
        context.appManager.getApps()
            .filter { context.favoritesManager.contains(it.packageName) }
}
