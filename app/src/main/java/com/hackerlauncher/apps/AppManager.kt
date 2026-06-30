package com.hackerlauncher.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager

class AppManager(private val context: Context) {

    private var cache: List<AppInfo> = emptyList()

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) = refreshCache()
    }

    init {
        refreshCache()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(packageReceiver, filter)
    }

    private fun refreshCache() {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        cache = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { info ->
                val launch = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                    ?: return@mapNotNull null
                AppInfo(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    launchIntent = launch
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun getApps(): List<AppInfo> = cache

    fun findApps(query: String): List<AppInfo> {
        if (query.isBlank()) return cache
        val lower = query.lowercase()
        return cache
            .filter { it.label.lowercase().contains(lower) }
            .sortedBy {
                val lbl = it.label.lowercase()
                when {
                    lbl == lower -> 0
                    lbl.startsWith(lower) -> 1
                    else -> 2
                }
            }
    }

    fun launch(app: AppInfo) {
        val intent = app.launchIntent.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
