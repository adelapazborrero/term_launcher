package com.hackerlauncher.commands

import com.hackerlauncher.commands.impl.AliasCommand
import com.hackerlauncher.commands.impl.AppsCommand
import com.hackerlauncher.commands.impl.CdCommand
import com.hackerlauncher.commands.impl.ClearCommand
import com.hackerlauncher.commands.impl.FavCommand
import com.hackerlauncher.commands.impl.HelpCommand
import com.hackerlauncher.commands.impl.InfoCommand
import com.hackerlauncher.commands.impl.MkdirCommand
import com.hackerlauncher.commands.impl.MvCommand
import com.hackerlauncher.commands.impl.OpenCommand
import com.hackerlauncher.commands.impl.SettingsCommand
import com.hackerlauncher.commands.impl.ThemesCommand

object CommandRegistry {

    private val commands: Map<String, Command> = buildMap {
        val open = OpenCommand()
        val apps = AppsCommand()
        val clear = ClearCommand()
        val help = HelpCommand()
        val fav = FavCommand()
        val alias = AliasCommand()

        put("open", open)
        put("launch", open)
        put("start", open)
        put("ls", apps)
        put("apps", apps)
        put("fav", fav)
        put("alias", alias)
        put("mkdir", MkdirCommand())
        put("cd", CdCommand())
        put("mv", MvCommand())
        put("themes", ThemesCommand())
        put("clear", clear)
        put("cls", clear)
        put("help", help)
        put("?", help)
        put("info", InfoCommand())
        put("settings", SettingsCommand())
    }

    fun get(name: String): Command? = commands[name.lowercase()]

    fun getUniqueNames(): List<String> {
        val seen = mutableSetOf<Command>()
        return commands.entries
            .filter { seen.add(it.value) }
            .map { it.key }
    }

    fun getUniqueDescriptions(): List<Pair<String, String>> {
        val seen = mutableSetOf<Command>()
        return commands.entries
            .filter { seen.add(it.value) }
            .map { it.key to it.value.description }
    }
}
