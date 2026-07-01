package com.termlauncher.commands.impl

import com.termlauncher.commands.Command
import com.termlauncher.commands.CommandContext
import com.termlauncher.settings.UiMode
import com.termlauncher.terminal.AliasSnapshot
import com.termlauncher.terminal.TerminalEntry

class AliasCommand : Command {
    override val description = "alias <name> <cmd> | alias list | alias remove <name>  — manage aliases"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        return when (args.firstOrNull()?.lowercase()) {
            "list" -> if (context.settingsManager.getAll().uiMode == UiMode.MODERN) {
                listOf(buildPanel(context))
            } else {
                listAliases(context)
            }
            "remove", "rm" -> removeAlias(args.drop(1), context)
            null -> if (context.settingsManager.getAll().uiMode == UiMode.MODERN) {
                listOf(buildPanel(context))
            } else {
                listOf(TerminalEntry.error("usage: alias <name> <cmd>  |  alias list  |  alias remove <name>"))
            }
            else -> setAlias(args, context)
        }
    }

    private fun buildPanel(context: CommandContext): TerminalEntry =
        TerminalEntry.aliasPanel(AliasSnapshot(context.aliasManager.getAll().toList()))

    private fun setAlias(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.size < 2) return listOf(TerminalEntry.error("usage: alias <name> <cmd>"))
        val name = args[0].lowercase()
        val expansion = args.drop(1).joinToString(" ")
        context.aliasManager.set(name, expansion)
        return listOf(TerminalEntry.output("alias set: $name → $expansion"))
    }

    private fun removeAlias(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val name = args.firstOrNull()?.lowercase()
            ?: return listOf(TerminalEntry.error("usage: alias remove <name>"))
        return if (context.aliasManager.remove(name)) {
            listOf(TerminalEntry.output("alias '$name' removed"))
        } else {
            listOf(TerminalEntry.error("alias '$name' not found"))
        }
    }

    private fun listAliases(context: CommandContext): List<TerminalEntry> {
        val all = context.aliasManager.getAll()
        if (all.isEmpty()) return listOf(TerminalEntry.info("no aliases set — use 'alias <name> <cmd>'"))
        return buildList {
            add(TerminalEntry.info("${all.size} alias(es):"))
            all.forEach { (name, expansion) ->
                add(TerminalEntry.output("  $name → $expansion"))
            }
        }
    }
}
