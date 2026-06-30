package com.hackerlauncher.apps

import android.content.Context

class FolderManager(context: Context) {

    private val prefs = context.getSharedPreferences("app_folders", Context.MODE_PRIVATE)

    fun getFolders(): List<String> =
        (prefs.getStringSet(KEY_FOLDERS, emptySet()) ?: emptySet()).sorted()

    fun folderExists(name: String) =
        name.lowercase() in (prefs.getStringSet(KEY_FOLDERS, emptySet()) ?: emptySet())

    fun createFolder(name: String): Boolean {
        val key = name.lowercase()
        val folders = getFolderSet().toMutableSet()
        if (key in folders) return false
        folders.add(key)
        prefs.edit().putStringSet(KEY_FOLDERS, folders).apply()
        return true
    }

    fun deleteFolder(name: String): Boolean {
        val key = name.lowercase()
        val folders = getFolderSet().toMutableSet()
        if (key !in folders) return false
        folders.remove(key)
        prefs.edit()
            .putStringSet(KEY_FOLDERS, folders)
            .remove(folderKey(key))
            .apply()
        return true
    }

    fun addApp(packageName: String, folder: String): Boolean {
        val key = folder.lowercase()
        if (!folderExists(key)) return false
        removeAppFromAllFolders(packageName)
        val apps = getAppsInFolder(key).toMutableSet()
        apps.add(packageName)
        prefs.edit().putStringSet(folderKey(key), apps).apply()
        return true
    }

    fun removeAppFromAllFolders(packageName: String) {
        val edit = prefs.edit()
        getFolders().forEach { folder ->
            val apps = getAppsInFolder(folder).toMutableSet()
            if (apps.remove(packageName)) edit.putStringSet(folderKey(folder), apps)
        }
        edit.apply()
    }

    fun getAppsInFolder(folder: String): Set<String> =
        prefs.getStringSet(folderKey(folder.lowercase()), emptySet()) ?: emptySet()

    fun getFolderForApp(packageName: String): String? =
        getFolders().firstOrNull { packageName in getAppsInFolder(it) }

    private fun getFolderSet(): Set<String> =
        prefs.getStringSet(KEY_FOLDERS, emptySet()) ?: emptySet()

    private fun folderKey(folder: String) = "folder_$folder"

    companion object {
        private const val KEY_FOLDERS = "folder_names"
    }
}
