package com.termlauncher.settings

enum class UiMode { TERMINAL, MODERN }

data class Settings(
    val fontSize: Float = 14f,
    val prompt: String = "root@hackr:~$ ",
    val uiMode: UiMode = UiMode.MODERN,
    val bgOpacity: Int = 100
)
