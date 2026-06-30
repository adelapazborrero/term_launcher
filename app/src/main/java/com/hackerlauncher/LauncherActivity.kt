package com.hackerlauncher

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hackerlauncher.databinding.ActivityLauncherBinding
import com.hackerlauncher.settings.Settings
import com.hackerlauncher.terminal.TerminalAdapter
import com.hackerlauncher.theme.HackerTheme
import kotlinx.coroutines.launch

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private val viewModel: LauncherViewModel by viewModels()
    private lateinit var adapter: TerminalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) { /* launchers don't exit on back */ }

        setupTerminal()
        setupInput()
        observeViewModel()
        showKeyboard()
    }

    private fun setupTerminal() {
        adapter = TerminalAdapter(
            theme = viewModel.currentTheme.value,
            fontSize = viewModel.currentSettings.value.fontSize
        )
        binding.terminalRecycler.apply {
            layoutManager = LinearLayoutManager(this@LauncherActivity).also {
                it.stackFromEnd = true
            }
            adapter = this@LauncherActivity.adapter
        }
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
    }

    private fun applyTheme(theme: HackerTheme) {
        binding.rootLayout.setBackgroundColor(theme.background)
        binding.promptText.setTextColor(theme.prompt)
        binding.commandInput.setTextColor(theme.foreground)
        binding.commandInput.setHintTextColor(theme.hint)
        adapter.updateTheme(theme)
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
