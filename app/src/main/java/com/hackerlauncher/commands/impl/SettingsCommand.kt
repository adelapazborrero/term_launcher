package com.hackerlauncher.commands.impl

import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry

class SettingsCommand : Command {
    override val description = "settings <list|set|reset> [key] [value]  — manage app settings"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        return when (args.firstOrNull()?.lowercase()) {
            "list", null -> listSettings(context)
            "set" -> setKey(args.drop(1), context)
            "reset" -> {
                context.settingsManager.reset()
                listOf(TerminalEntry.output("settings reset to defaults"))
            }
            else -> listOf(TerminalEntry.error("usage: settings <list|set|reset> [key] [value]"))
        }
    }

    private fun listSettings(context: CommandContext): List<TerminalEntry> {
        val s = context.settingsManager.getAll()
        return buildList {
            add(TerminalEntry.info("current settings:"))
            add(TerminalEntry.output("  font_size  = ${s.fontSize.toInt()}sp"))
            add(TerminalEntry.output("  prompt     = ${s.prompt}"))
            add(TerminalEntry.info("use 'settings set <key> <value>' to change"))
            add(TerminalEntry.info("keys: font_size (8-32), prompt"))
        }
    }

    private fun setKey(args: List<String>, context: CommandContext): List<TerminalEntry> {
        if (args.size < 2) return listOf(TerminalEntry.error("usage: settings set <key> <value>"))
        val key = args[0]
        val value = args.drop(1).joinToString(" ")
        return if (context.settingsManager.set(key, value)) {
            listOf(TerminalEntry.output("$key set to: $value"))
        } else {
            when (key) {
                "font_size" -> listOf(TerminalEntry.error("font_size must be a number between 8 and 32"))
                else -> listOf(TerminalEntry.error("unknown key '$key' — valid keys: font_size, prompt"))
            }
        }
    }
}
