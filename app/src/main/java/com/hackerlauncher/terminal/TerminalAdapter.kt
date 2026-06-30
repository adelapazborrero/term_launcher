package com.hackerlauncher.terminal

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hackerlauncher.R
import com.hackerlauncher.theme.HackerTheme

class TerminalAdapter(
    private var entries: List<TerminalEntry> = emptyList(),
    private var theme: HackerTheme
) : RecyclerView.Adapter<TerminalAdapter.ViewHolder>() {

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_terminal_entry, parent, false) as TextView
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.textView.text = entry.text
        holder.textView.setTextColor(
            when (entry.type) {
                TerminalEntry.Type.INPUT -> theme.prompt
                TerminalEntry.Type.OUTPUT -> theme.foreground
                TerminalEntry.Type.ERROR -> theme.error
                TerminalEntry.Type.INFO -> theme.info
            }
        )
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
}
