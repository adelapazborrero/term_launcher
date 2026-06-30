package com.hackerlauncher.commands

import android.content.Context

class AliasManager(context: Context) {

    private val prefs = context.getSharedPreferences("aliases", Context.MODE_PRIVATE)

    fun resolve(name: String): String? = prefs.getString(name, null)

    fun set(name: String, expansion: String) {
        prefs.edit().putString(name, expansion).apply()
    }

    fun remove(name: String): Boolean {
        if (!prefs.contains(name)) return false
        prefs.edit().remove(name).apply()
        return true
    }

    @Suppress("UNCHECKED_CAST")
    fun getAll(): Map<String, String> =
        (prefs.all as Map<String, String>).toSortedMap()
}
