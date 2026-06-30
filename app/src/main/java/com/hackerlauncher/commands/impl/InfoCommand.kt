package com.hackerlauncher.commands.impl

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import com.hackerlauncher.commands.Command
import com.hackerlauncher.commands.CommandContext
import com.hackerlauncher.terminal.TerminalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class InfoCommand : Command {
    override val description = "info  — show device info (model, OS, battery, uptime)"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalEntry> {
        val ctx = context.androidContext
        return buildList {
            add(TerminalEntry.info("--- system info ---"))
            add(TerminalEntry.output("device   : ${Build.MANUFACTURER} ${Build.MODEL}"))
            add(TerminalEntry.output("android  : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"))
            add(TerminalEntry.output("time     : ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"))
            add(TerminalEntry.output("uptime   : ${formatUptime(SystemClock.elapsedRealtime())}"))
            val battery = getBatteryLevel(ctx)
            if (battery >= 0) add(TerminalEntry.output("battery  : $battery%"))
        }
    }

    private fun getBatteryLevel(context: android.content.Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return -1
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    }

    private fun formatUptime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return "${hours}h ${minutes}m"
    }
}
