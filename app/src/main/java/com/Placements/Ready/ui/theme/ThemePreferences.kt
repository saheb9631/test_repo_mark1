package com.Placements.Ready.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    val isDarkMode = mutableStateOf(prefs.getBoolean("dark_mode", false))

    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }
}
