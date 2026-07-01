package com.termlauncher.terminal

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.termlauncher.R
import com.termlauncher.settings.UiMode
import com.termlauncher.theme.HackerTheme
import com.termlauncher.theme.Themes

class TerminalAdapter(
    private var entries: List<TerminalEntry> = emptyList(),
    private var theme: HackerTheme,
    private var fontSize: Float = 14f,
    private var uiMode: UiMode = UiMode.TERMINAL,
    private val onLaunchApp: (String) -> Unit = {},
    private val onAppInfo: (String) -> Unit = {},
    private val onToggleFavorite: (String) -> Unit = {},
    private val isFavorite: (String) -> Boolean = { false },
    private val iconFor: (String) -> Drawable? = { null },
    private val onSaveSettings: (id: Long, theme: String, uiMode: UiMode, fontSize: Float, prompt: String) -> Unit = { _, _, _, _, _ -> },
    private val onSaveAliases: (id: Long, original: List<Pair<String, String>>, updated: List<Pair<String, String>>) -> Unit = { _, _, _ -> },
    private val onClosePanel: (id: Long) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class TextViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    class ModernInputViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val indicator: TextView = root.findViewById(R.id.status_indicator)
        val commandText: TextView = root.findViewById(R.id.command_text)
        val time: TextView = root.findViewById(R.id.command_time)
    }

    class DividerViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    class SettingsPanelViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val themeContainer: LinearLayout = root.findViewById(R.id.theme_chip_container)
        val modeTerminalBtn: TextView = root.findViewById(R.id.mode_terminal_btn)
        val modeModernBtn: TextView = root.findViewById(R.id.mode_modern_btn)
        val fontMinus: TextView = root.findViewById(R.id.font_minus)
        val fontPlus: TextView = root.findViewById(R.id.font_plus)
        val fontValue: TextView = root.findViewById(R.id.font_value)
        val promptInput: EditText = root.findViewById(R.id.prompt_input)
        val cancelBtn: TextView = root.findViewById(R.id.cancel_btn)
        val saveBtn: TextView = root.findViewById(R.id.save_btn)
        var draftTheme: String = ""
        var draftMode: UiMode = UiMode.TERMINAL
        var draftFontSize: Float = 14f
    }

    class AliasPanelViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val rowsContainer: LinearLayout = root.findViewById(R.id.alias_rows_container)
        val emptyState: TextView = root.findViewById(R.id.empty_state)
        val newNameInput: EditText = root.findViewById(R.id.new_name_input)
        val newCmdInput: EditText = root.findViewById(R.id.new_cmd_input)
        val addBtn: TextView = root.findViewById(R.id.add_btn)
        val cancelBtn: TextView = root.findViewById(R.id.cancel_btn)
        val saveBtn: TextView = root.findViewById(R.id.save_btn)
        var draft: MutableList<Pair<String, String>> = mutableListOf()
    }

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
            entry.type == TerminalEntry.Type.SETTINGS_PANEL && uiMode == UiMode.MODERN -> VIEW_SETTINGS_PANEL
            entry.type == TerminalEntry.Type.ALIAS_PANEL && uiMode == UiMode.MODERN -> VIEW_ALIAS_PANEL
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
            VIEW_SETTINGS_PANEL -> SettingsPanelViewHolder(
                inflater.inflate(R.layout.item_settings_panel_modern, parent, false)
            )
            VIEW_ALIAS_PANEL -> AliasPanelViewHolder(
                inflater.inflate(R.layout.item_alias_panel_modern, parent, false)
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
            is SettingsPanelViewHolder -> bindSettingsPanel(holder, entry)
            is AliasPanelViewHolder -> bindAliasPanel(holder, entry)
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

    private fun bindSettingsPanel(holder: SettingsPanelViewHolder, entry: TerminalEntry) {
        val snap = entry.settingsSnapshot ?: return
        val dp = holder.itemView.resources.displayMetrics.density
        holder.itemView.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * dp
            setStroke((1.5f * dp).toInt(), theme.prompt)
            setColor(theme.background)
        }

        fun render() {
            holder.themeContainer.removeAllViews()
            snap.availableThemes.forEach { name ->
                val chipTheme = Themes.ALL.find { it.name == name }
                val selected = name == holder.draftTheme
                val accent = chipTheme?.foreground ?: theme.foreground
                val chip = TextView(holder.itemView.context).apply {
                    text = name
                    textSize = fontSize * 0.9f
                    setPadding((10 * dp).toInt(), (4 * dp).toInt(), (10 * dp).toInt(), (4 * dp).toInt())
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 6f * dp
                        setStroke((1f * dp).toInt(), accent)
                        setColor(if (selected) accent else theme.background)
                    }
                    setTextColor(if (selected) theme.background else accent)
                    setOnClickListener {
                        holder.draftTheme = name
                        render()
                    }
                }
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = (6 * dp).toInt() }
                holder.themeContainer.addView(chip, lp)
            }

            listOf(holder.modeTerminalBtn to UiMode.TERMINAL, holder.modeModernBtn to UiMode.MODERN).forEach { (btn, mode) ->
                val selected = holder.draftMode == mode
                btn.textSize = fontSize * 0.95f
                btn.setTextColor(if (selected) theme.prompt else theme.hint)
                btn.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            }

            holder.fontValue.textSize = fontSize * 0.95f
            holder.fontValue.setTextColor(theme.foreground)
            holder.fontValue.text = "${holder.draftFontSize.toInt()}sp"
        }

        holder.draftTheme = snap.theme
        holder.draftMode = snap.uiMode
        holder.draftFontSize = snap.fontSize
        holder.promptInput.setText(snap.prompt)
        holder.promptInput.textSize = fontSize * 0.95f
        holder.promptInput.setTextColor(theme.foreground)
        render()

        holder.modeTerminalBtn.setOnClickListener { holder.draftMode = UiMode.TERMINAL; render() }
        holder.modeModernBtn.setOnClickListener { holder.draftMode = UiMode.MODERN; render() }

        holder.fontMinus.setTextColor(theme.prompt)
        holder.fontPlus.setTextColor(theme.prompt)
        holder.fontMinus.setOnClickListener {
            holder.draftFontSize = (holder.draftFontSize - 1f).coerceAtLeast(8f)
            render()
        }
        holder.fontPlus.setOnClickListener {
            holder.draftFontSize = (holder.draftFontSize + 1f).coerceAtMost(32f)
            render()
        }

        holder.cancelBtn.setTextColor(theme.hint)
        holder.cancelBtn.setOnClickListener { onClosePanel(entry.id) }

        holder.saveBtn.setTextColor(theme.prompt)
        holder.saveBtn.setOnClickListener {
            onSaveSettings(entry.id, holder.draftTheme, holder.draftMode, holder.draftFontSize, holder.promptInput.text.toString())
        }
    }

    private fun bindAliasPanel(holder: AliasPanelViewHolder, entry: TerminalEntry) {
        val snap = entry.aliasSnapshot ?: return
        val dp = holder.itemView.resources.displayMetrics.density
        holder.itemView.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * dp
            setStroke((1.5f * dp).toInt(), theme.prompt)
            setColor(theme.background)
        }

        fun render() {
            holder.rowsContainer.removeAllViews()
            holder.emptyState.visibility = if (holder.draft.isEmpty()) View.VISIBLE else View.GONE
            holder.emptyState.setTextColor(theme.hint)
            holder.draft.forEach { (name, expansion) ->
                val row = LinearLayout(holder.itemView.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(0, (2 * dp).toInt(), 0, (2 * dp).toInt())
                }
                val label = TextView(holder.itemView.context).apply {
                    text = "$name → $expansion"
                    textSize = fontSize * 0.9f
                    setTextColor(theme.foreground)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
                val labelLp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                row.addView(label, labelLp)

                val remove = TextView(holder.itemView.context).apply {
                    text = "✕"
                    textSize = fontSize * 0.9f
                    setTextColor(theme.error)
                    setPadding((10 * dp).toInt(), 0, 0, 0)
                    setOnClickListener {
                        holder.draft.removeAll { it.first == name }
                        render()
                    }
                }
                row.addView(remove)
                holder.rowsContainer.addView(row)
            }
        }

        holder.draft = snap.aliases.toMutableList()
        holder.newNameInput.setText("")
        holder.newCmdInput.setText("")
        holder.newNameInput.textSize = fontSize * 0.9f
        holder.newCmdInput.textSize = fontSize * 0.9f
        holder.newNameInput.setTextColor(theme.foreground)
        holder.newCmdInput.setTextColor(theme.foreground)
        render()

        holder.addBtn.setOnClickListener {
            val name = holder.newNameInput.text.toString().trim().lowercase()
            val cmd = holder.newCmdInput.text.toString().trim()
            if (name.isNotEmpty() && cmd.isNotEmpty()) {
                holder.draft.removeAll { it.first == name }
                holder.draft.add(name to cmd)
                holder.newNameInput.setText("")
                holder.newCmdInput.setText("")
                render()
            }
        }

        holder.cancelBtn.setTextColor(theme.hint)
        holder.cancelBtn.setOnClickListener { onClosePanel(entry.id) }

        holder.saveBtn.setTextColor(theme.prompt)
        holder.saveBtn.setOnClickListener {
            onSaveAliases(entry.id, snap.aliases, holder.draft.toList())
        }
    }

    private fun colorFor(entry: TerminalEntry) = when (entry.type) {
        TerminalEntry.Type.INPUT -> theme.prompt
        TerminalEntry.Type.OUTPUT -> theme.foreground
        TerminalEntry.Type.ERROR -> theme.error
        TerminalEntry.Type.INFO -> theme.info
        TerminalEntry.Type.DIVIDER -> theme.foreground
        TerminalEntry.Type.SETTINGS_PANEL -> theme.foreground
        TerminalEntry.Type.ALIAS_PANEL -> theme.foreground
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
        private const val VIEW_SETTINGS_PANEL = 4
        private const val VIEW_ALIAS_PANEL = 5
    }
}
