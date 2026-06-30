package com.hackerlauncher.terminal

data class TerminalEntry(val text: String, val type: Type) {
    enum class Type { INPUT, OUTPUT, ERROR, INFO }

    companion object {
        fun output(text: String) = TerminalEntry(text, Type.OUTPUT)
        fun error(text: String) = TerminalEntry(text, Type.ERROR)
        fun info(text: String) = TerminalEntry(text, Type.INFO)
        fun input(text: String) = TerminalEntry(text, Type.INPUT)
    }
}
