package com.termlauncher.commands

import com.termlauncher.commands.impl.AliasCommand
import com.termlauncher.commands.impl.AppsCommand
import com.termlauncher.commands.impl.AppsListCommand
import com.termlauncher.commands.impl.CdCommand
import com.termlauncher.commands.impl.ClearCommand
import com.termlauncher.commands.impl.FavCommand
import com.termlauncher.commands.impl.FindCommand
import com.termlauncher.commands.impl.HelpCommand
import com.termlauncher.commands.impl.InfoCommand
import com.termlauncher.commands.impl.MkdirCommand
import com.termlauncher.commands.impl.MvCommand
import com.termlauncher.commands.impl.OpenCommand
import com.termlauncher.commands.impl.SettingsCommand
import com.termlauncher.commands.impl.ThemesCommand

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
        put("apps", AppsListCommand())
        put("find", FindCommand())
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
