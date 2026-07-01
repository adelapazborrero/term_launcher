package com.termlauncher.apps

import android.content.Context

class FolderManager(context: Context) {

    private val prefs = context.getSharedPreferences("app_folders", Context.MODE_PRIVATE)

    fun getFolders(): List<String> =
        (prefs.getStringSet(KEY_FOLDERS, emptySet()) ?: emptySet()).sorted()

    fun folderExists(name: String) =
        name.lowercase() in (prefs.getStringSet(KEY_FOLDERS, emptySet()) ?: emptySet())

    fun createFolder(name: String): Boolean {
        val key = name.lowercase()
        val folders = (prefs.getStringSet(KEY_FOLDERS, emptySet()) ?: emptySet()).toMutableSet()
        if (key in folders) return false
        folders.add(key)
        prefs.edit().putStringSet(KEY_FOLDERS, folders).commit()
        return true
    }

    fun deleteFolder(name: String): Boolean {
        val key = name.lowercase()
        val folders = (prefs.getStringSet(KEY_FOLDERS, emptySet()) ?: emptySet()).toMutableSet()
        if (key !in folders) return false
        folders.remove(key)
        prefs.edit()
            .putStringSet(KEY_FOLDERS, folders)
            .remove(folderKey(key))
            .commit()
        return true
    }

    fun addApp(packageName: String, folder: String): Boolean {
        val key = folder.lowercase()
        if (!folderExists(key)) return false
        val edit = prefs.edit()
        // Remove from every folder and add to target in one atomic write
        getFolders().forEach { f ->
            val apps = getAppsInFolder(f).toMutableSet()
            if (apps.remove(packageName)) edit.putStringSet(folderKey(f), apps)
        }
        val target = getAppsInFolder(key).toMutableSet()
        target.add(packageName)
        edit.putStringSet(folderKey(key), target)
        edit.commit()
        return true
    }

    fun removeAppFromAllFolders(packageName: String) {
        val edit = prefs.edit()
        getFolders().forEach { folder ->
            val apps = getAppsInFolder(folder).toMutableSet()
            if (apps.remove(packageName)) edit.putStringSet(folderKey(folder), apps)
        }
        edit.commit()
    }

    fun getAppsInFolder(folder: String): Set<String> =
        prefs.getStringSet(folderKey(folder.lowercase()), emptySet()) ?: emptySet()

    fun getFolderForApp(packageName: String): String? =
        getFolders().firstOrNull { packageName in getAppsInFolder(it) }

    companion object {
        private const val KEY_FOLDERS = "folder_names"
        private fun folderKey(folder: String) = "folder_$folder"
    }
}
