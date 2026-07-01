package com.termlauncher.terminal

data class TerminalEntry(
    val text: String,
    val type: Type,
    val timestamp: String? = null,
    val success: Boolean = true,
    val packageName: String? = null
) {
    enum class Type { INPUT, OUTPUT, ERROR, INFO, DIVIDER }

    companion object {
        fun output(text: String) = TerminalEntry(text, Type.OUTPUT)
        fun error(text: String) = TerminalEntry(text, Type.ERROR, success = false)
        fun info(text: String) = TerminalEntry(text, Type.INFO)
        fun input(text: String, timestamp: String? = null, success: Boolean = true) =
            TerminalEntry(text, Type.INPUT, timestamp, success)
        fun divider() = TerminalEntry("", Type.DIVIDER)
        fun app(text: String, packageName: String) = TerminalEntry(text, Type.OUTPUT, packageName = packageName)
    }
}
