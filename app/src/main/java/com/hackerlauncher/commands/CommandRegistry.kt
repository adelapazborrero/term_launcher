package com.hackerlauncher.commands

import com.hackerlauncher.commands.impl.AppsCommand
import com.hackerlauncher.commands.impl.ClearCommand
import com.hackerlauncher.commands.impl.FavCommand
import com.hackerlauncher.commands.impl.HelpCommand
import com.hackerlauncher.commands.impl.InfoCommand
import com.hackerlauncher.commands.impl.OpenCommand
import com.hackerlauncher.commands.impl.ThemeCommand
import com.hackerlauncher.commands.impl.ThemesCommand

object CommandRegistry {

    private val commands: Map<String, Command> = buildMap {
        val open = OpenCommand()
        val apps = AppsCommand()
        val clear = ClearCommand()
        val help = HelpCommand()

        val fav = FavCommand()

        put("open", open)
        put("launch", open)
        put("start", open)
        put("apps", apps)
        put("fav", fav)
        put("ls", apps)
        put("theme", ThemeCommand())
        put("themes", ThemesCommand())
        put("clear", clear)
        put("cls", clear)
        put("help", help)
        put("?", help)
        put("info", InfoCommand())
    }

    fun get(name: String): Command? = commands[name.lowercase()]

    fun getUniqueDescriptions(): List<Pair<String, String>> {
        val seen = mutableSetOf<Command>()
        return commands.entries
            .filter { seen.add(it.value) }
            .map { it.key to it.value.description }
    }
}
