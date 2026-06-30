package com.hackerlauncher.apps

import android.content.Context

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun getPackageNames(): Set<String> = prefs.getStringSet(KEY, emptySet()) ?: emptySet()

    fun add(packageName: String) {
        val current = getPackageNames().toMutableSet()
        current.add(packageName)
        prefs.edit().putStringSet(KEY, current).apply()
    }

    fun remove(packageName: String) {
        val current = getPackageNames().toMutableSet()
        current.remove(packageName)
        prefs.edit().putStringSet(KEY, current).apply()
    }

    fun contains(packageName: String) = packageName in getPackageNames()

    companion object {
        private const val KEY = "fav_packages"
    }
}
