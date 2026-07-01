package com.termlauncher.commands

import com.termlauncher.terminal.TerminalEntry

class CommandProcessor(private val context: CommandContext) {

    fun process(input: String, depth: Int = 0): List<TerminalEntry> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return emptyList()

        val tokens = tokenize(trimmed)
        val commandName = tokens.first().lowercase()
        val args = tokens.drop(1)

        if (depth < 5) {
            val expansion = context.aliasManager.resolve(commandName)
            if (expansion != null) {
                val expanded = if (args.isEmpty()) expansion else "$expansion ${args.joinToString(" ")}"
                return process(expanded, depth + 1)
            }
        }

        val command = CommandRegistry.get(commandName)
            ?: return listOf(
                TerminalEntry.error("command not found: $commandName"),
                TerminalEntry.info("type 'help' for available commands")
            )

        if (args.firstOrNull() == "-h") {
            return listOf(TerminalEntry.info(command.description))
        }

        return try {
            command.execute(args, context)
        } catch (e: Exception) {
            listOf(TerminalEntry.error("error: ${e.message ?: "unknown error"}"))
        }
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (char in input) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ' ' && !inQuotes -> {
                    if (current.isNotEmpty()) { tokens.add(current.toString()); current.clear() }
                }
                else -> current.append(char)
            }
        }
        if (current.isNotEmpty()) tokens.add(current.toString())
        return tokens
    }
}
