package com.termlauncher.theme

import android.graphics.Color

data class HackerTheme(
    val name: String,
    val background: Int,
    val foreground: Int,
    val prompt: Int,
    val error: Int,
    val info: Int,
    val hint: Int
)

object Themes {
    val MATRIX = HackerTheme(
        name = "matrix",
        background = Color.parseColor("#000000"),
        foreground = Color.parseColor("#00FF41"),
        prompt = Color.parseColor("#00CC33"),
        error = Color.parseColor("#FF3333"),
        info = Color.parseColor("#007700"),
        hint = Color.parseColor("#004400")
    )

    val BLOOD = HackerTheme(
        name = "blood",
        background = Color.parseColor("#1A0000"),
        foreground = Color.parseColor("#FF2222"),
        prompt = Color.parseColor("#FF5555"),
        error = Color.parseColor("#FF8800"),
        info = Color.parseColor("#880000"),
        hint = Color.parseColor("#550000")
    )

    val ICE = HackerTheme(
        name = "ice",
        background = Color.parseColor("#001122"),
        foreground = Color.parseColor("#88CCFF"),
        prompt = Color.parseColor("#66AAFF"),
        error = Color.parseColor("#FF4444"),
        info = Color.parseColor("#4488AA"),
        hint = Color.parseColor("#224466")
    )

    val AMBER = HackerTheme(
        name = "amber",
        background = Color.parseColor("#1A1000"),
        foreground = Color.parseColor("#FFAA00"),
        prompt = Color.parseColor("#FFCC33"),
        error = Color.parseColor("#FF4444"),
        info = Color.parseColor("#886600"),
        hint = Color.parseColor("#554400")
    )

    val GHOST = HackerTheme(
        name = "ghost",
        background = Color.parseColor("#0A0A0A"),
        foreground = Color.parseColor("#CCCCCC"),
        prompt = Color.parseColor("#FFFFFF"),
        error = Color.parseColor("#FF3333"),
        info = Color.parseColor("#888888"),
        hint = Color.parseColor("#444444")
    )

    val ALL = listOf(MATRIX, BLOOD, ICE, AMBER, GHOST)
}
