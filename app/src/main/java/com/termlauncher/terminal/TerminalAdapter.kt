package com.termlauncher.terminal

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.termlauncher.R
import com.termlauncher.settings.UiMode
import com.termlauncher.theme.HackerTheme

class TerminalAdapter(
    private var entries: List<TerminalEntry> = emptyList(),
    private var theme: HackerTheme,
    private var fontSize: Float = 14f,
    private var uiMode: UiMode = UiMode.TERMINAL,
    private val onLaunchApp: (String) -> Unit = {},
    private val onAppInfo: (String) -> Unit = {},
    private val onToggleFavorite: (String) -> Unit = {},
    private val isFavorite: (String) -> Boolean = { false },
    private val iconFor: (String) -> Drawable? = { null }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class TextViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    class ModernInputViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val indicator: TextView = root.findViewById(R.id.status_indicator)
        val commandText: TextView = root.findViewById(R.id.command_text)
        val time: TextView = root.findViewById(R.id.command_time)
    }

    class DividerViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    class AppEntryViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val icon: ImageView = root.findViewById(R.id.app_icon)
        val label: TextView = root.findViewById(R.id.app_label)
        val playButton: TextView = root.findViewById(R.id.play_button)
        val infoButton: TextView = root.findViewById(R.id.info_button)
        val favButton: TextView = root.findViewById(R.id.fav_button)
    }

    override fun getItemViewType(position: Int): Int {
        val entry = entries[position]
        return when {
            entry.type == TerminalEntry.Type.DIVIDER -> VIEW_DIVIDER
            entry.type == TerminalEntry.Type.INPUT && uiMode == UiMode.MODERN -> VIEW_INPUT_MODERN
            entry.packageName != null && uiMode == UiMode.MODERN -> VIEW_APP_ENTRY_MODERN
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
            VIEW_APP_ENTRY_MODERN -> AppEntryViewHolder(
                inflater.inflate(R.layout.item_app_entry_modern, parent, false)
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
            is AppEntryViewHolder -> bindAppEntry(holder, entry)
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

    private fun bindAppEntry(holder: AppEntryViewHolder, entry: TerminalEntry) {
        val pkg = entry.packageName ?: return
        val dp = holder.itemView.resources.displayMetrics.density
        holder.itemView.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f * dp
            setStroke((1f * dp).toInt(), theme.hint)
            setColor(theme.background)
        }

        holder.icon.setImageDrawable(iconFor(pkg))

        holder.label.textSize = fontSize
        holder.label.text = entry.text.trim()
        holder.label.setTextColor(theme.foreground)

        holder.playButton.textSize = fontSize
        holder.playButton.setTextColor(theme.prompt)
        holder.playButton.setOnClickListener { onLaunchApp(pkg) }

        holder.infoButton.textSize = fontSize
        holder.infoButton.setTextColor(theme.hint)
        holder.infoButton.setOnClickListener { onAppInfo(pkg) }

        holder.favButton.textSize = fontSize
        fun renderFav(favorited: Boolean) {
            holder.favButton.text = if (favorited) "★" else "☆"
            holder.favButton.setTextColor(if (favorited) theme.prompt else theme.hint)
        }
        renderFav(isFavorite(pkg))
        holder.favButton.setOnClickListener {
            onToggleFavorite(pkg)
            renderFav(isFavorite(pkg))
        }
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
        private const val VIEW_APP_ENTRY_MODERN = 3
    }
}
