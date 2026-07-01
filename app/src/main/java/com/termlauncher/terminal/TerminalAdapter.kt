package com.hackerlauncher.terminal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hackerlauncher.R
import com.hackerlauncher.settings.UiMode
import com.hackerlauncher.theme.HackerTheme

class TerminalAdapter(
    private var entries: List<TerminalEntry> = emptyList(),
    private var theme: HackerTheme,
    private var fontSize: Float = 14f,
    private var uiMode: UiMode = UiMode.TERMINAL
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class TextViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    class ModernInputViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val indicator: TextView = root.findViewById(R.id.status_indicator)
        val commandText: TextView = root.findViewById(R.id.command_text)
        val time: TextView = root.findViewById(R.id.command_time)
    }

    class DividerViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun getItemViewType(position: Int): Int {
        val entry = entries[position]
        return when {
            entry.type == TerminalEntry.Type.DIVIDER -> VIEW_DIVIDER
            entry.type == TerminalEntry.Type.INPUT && uiMode == UiMode.MODERN -> VIEW_INPUT_MODERN
            else -> VIEW_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_INPUT_MODERN -> ModernInputViewHolder(
                inflater.inflate(R.layout.item_input_modern, parent, false)
            )
            VIEW_DIVIDER -> DividerViewHolder(
                inflater.inflate(R.layout.item_divider, parent, false)
            )
            else -> TextViewHolder(
                inflater.inflate(R.layout.item_terminal_entry, parent, false) as TextView
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val entry = entries[position]
        when (holder) {
            is TextViewHolder -> bindText(holder, entry)
            is ModernInputViewHolder -> bindModernInput(holder, entry)
            is DividerViewHolder -> holder.view.setBackgroundColor(theme.foreground)
        }
    }

    private fun bindText(holder: TextViewHolder, entry: TerminalEntry) {
        holder.textView.textSize = fontSize
        holder.textView.text = when (entry.type) {
            TerminalEntry.Type.INPUT -> "$ ${entry.text}"
            else -> entry.text
        }
        holder.textView.setTextColor(colorFor(entry))
    }

    private fun bindModernInput(holder: ModernInputViewHolder, entry: TerminalEntry) {
        val indicatorColor = if (entry.success) theme.foreground else theme.error
        holder.indicator.setTextColor(indicatorColor)
        holder.indicator.textSize = fontSize
        holder.commandText.text = entry.text
        holder.commandText.textSize = fontSize
        holder.commandText.setTextColor(theme.prompt)
        holder.time.text = entry.timestamp ?: ""
        holder.time.textSize = fontSize * 0.8f
        holder.time.setTextColor(theme.hint)
    }

    private fun colorFor(entry: TerminalEntry) = when (entry.type) {
        TerminalEntry.Type.INPUT -> theme.prompt
        TerminalEntry.Type.OUTPUT -> theme.foreground
        TerminalEntry.Type.ERROR -> theme.error
        TerminalEntry.Type.INFO -> theme.info
        TerminalEntry.Type.DIVIDER -> theme.foreground
    }

    override fun getItemCount() = entries.size

    fun updateEntries(newEntries: List<TerminalEntry>) {
        val oldSize = entries.size
        entries = newEntries
        if (newEntries.size > oldSize) {
            notifyItemRangeInserted(oldSize, newEntries.size - oldSize)
        } else {
            notifyDataSetChanged()
        }
    }

    fun updateTheme(newTheme: HackerTheme) {
        theme = newTheme
        notifyDataSetChanged()
    }

    fun updateFontSize(sp: Float) {
        fontSize = sp
        notifyDataSetChanged()
    }

    fun updateUiMode(mode: UiMode) {
        uiMode = mode
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TEXT = 0
        private const val VIEW_INPUT_MODERN = 1
        private const val VIEW_DIVIDER = 2
    }
}
