package com.hackerlauncher.apps

import android.content.Intent

data class AppInfo(
    val label: String,
    val packageName: String,
    val launchIntent: Intent
)
