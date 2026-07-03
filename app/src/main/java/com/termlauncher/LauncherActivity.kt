package com.termlauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import android.graphics.drawable.GradientDrawable
import com.termlauncher.databinding.ActivityLauncherBinding
import com.termlauncher.settings.Settings
import com.termlauncher.settings.UiMode
import com.termlauncher.terminal.TerminalAdapter
import com.termlauncher.theme.HackerTheme
import kotlinx.coroutines.launch

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private val viewModel: LauncherViewModel by viewModels()
    private lateinit var adapter: TerminalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) { /* launchers don't exit on back */ }

        setupInsets()
        setupTerminal()
        setupInput()
        observeViewModel()
        showKeyboard()
    }

    /**
     * On Android 15 (API 35) edge-to-edge is enforced, so the window draws behind
     * the system bars and IME and `adjustResize` no longer shrinks the layout. We
     * opt out of decor-fitting and instead pad the root by the system bar + keyboard
     * insets ourselves, keeping the input bar above the keyboard on every version.
     */
    private fun setupInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val basePad = (8 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(
                basePad + bars.left,
                basePad + bars.top,
                basePad + bars.right,
                basePad + maxOf(bars.bottom, ime.bottom)
            )
            insets
        }
    }

    private fun setupTerminal() {
        adapter = TerminalAdapter(
            theme = viewModel.currentTheme.value,
            fontSize = viewModel.currentSettings.value.fontSize,
            uiMode = viewModel.currentSettings.value.uiMode,
            onLaunchApp = { packageName -> viewModel.launchApp(packageName) },
            onAppInfo = { packageName -> openAppInfo(packageName) },
            onToggleFavorite = { packageName -> viewModel.toggleFavorite(packageName) },
            isFavorite = { packageName -> viewModel.isFavorite(packageName) },
            iconFor = { packageName -> viewModel.iconFor(packageName) },
            onSaveSettings = { id, theme, mode, fontSize, prompt, bgOpacity ->
                viewModel.applySettings(id, theme, mode, fontSize, prompt, bgOpacity)
            },
            onSaveAliases = { id, original, updated -> viewModel.applyAliases(id, original, updated) },
            onClosePanel = { id -> viewModel.closePanel(id) },
            onSelectTheme = { name -> viewModel.selectTheme(name) }
        )
        binding.terminalRecycler.apply {
            layoutManager = LinearLayoutManager(this@LauncherActivity).also {
                it.stackFromEnd = true
            }
            adapter = this@LauncherActivity.adapter
        }
        applyRecyclerAnimator(viewModel.currentSettings.value.uiMode)
    }

    private fun applyRecyclerAnimator(mode: UiMode) {
        binding.terminalRecycler.itemAnimator = if (mode == UiMode.MODERN) DefaultItemAnimator() else null
    }

    private fun openAppInfo(packageName: String) {
        val intent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun setupInput() {
        binding.commandInput.setOnEditorActionListener { _, actionId, event ->
            val isDone = actionId == EditorInfo.IME_ACTION_DONE
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER
                    && event.action == KeyEvent.ACTION_DOWN
            if (isDone || isEnter) { submitCommand(); true } else false
        }
    }

    private fun submitCommand() {
        val input = binding.commandInput.text?.toString() ?: return
        binding.commandInput.text?.clear()
        viewModel.submit(input)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                adapter.updateEntries(entries)
                if (entries.isNotEmpty()) {
                    binding.terminalRecycler.scrollToPosition(entries.size - 1)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.currentTheme.collect { theme -> applyTheme(theme) }
        }
        lifecycleScope.launch {
            viewModel.currentSettings.collect { settings -> applySettings(settings) }
        }
    }

    private fun applySettings(settings: Settings) {
        binding.promptText.text = settings.prompt
        binding.promptText.textSize = settings.fontSize
        binding.commandInput.textSize = settings.fontSize
        adapter.updateFontSize(settings.fontSize)
        adapter.updateUiMode(settings.uiMode)
        applyInputBarStyle(settings.uiMode)
        applyRecyclerAnimator(settings.uiMode)
        applyRootBackground(viewModel.currentTheme.value, settings.bgOpacity)
    }

    private fun applyInputBarStyle(mode: UiMode) {
        val theme = viewModel.currentTheme.value
        val dp = resources.displayMetrics.density
        if (mode == UiMode.MODERN) {
            val stroke = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10f * dp
                setStroke((1.5f * dp).toInt(), theme.prompt)
                setColor(colorWithOpacity(theme.background, viewModel.currentSettings.value.bgOpacity))
            }
            binding.inputBar.background = stroke
            val hPad = (14 * dp).toInt()
            val vPad = (8 * dp).toInt()
            binding.inputBar.setPadding(hPad, vPad, hPad, vPad)
        } else {
            binding.inputBar.background = null
            binding.inputBar.setPadding(0, 0, 0, 0)
        }
    }

    private fun applyTheme(theme: HackerTheme) {
        applyRootBackground(theme, viewModel.currentSettings.value.bgOpacity)
        binding.promptText.setTextColor(theme.prompt)
        binding.commandInput.setTextColor(theme.foreground)
        binding.commandInput.setHintTextColor(theme.hint)
        adapter.updateTheme(theme)
        applyInputBarStyle(viewModel.currentSettings.value.uiMode)
    }

    private fun applyRootBackground(theme: HackerTheme, opacityPercent: Int) {
        binding.rootLayout.setBackgroundColor(colorWithOpacity(theme.background, opacityPercent))
    }

    private fun colorWithOpacity(color: Int, opacityPercent: Int): Int {
        val alpha = (opacityPercent.coerceIn(0, 100) * 255) / 100
        return (color and 0x00FFFFFF) or (alpha shl 24)
    }

    private fun showKeyboard() {
        binding.commandInput.requestFocus()
        binding.commandInput.postDelayed({
            val imm = getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(binding.commandInput, InputMethodManager.SHOW_IMPLICIT)
        }, 150)
    }

    override fun onResume() {
        super.onResume()
        showKeyboard()
    }
}
