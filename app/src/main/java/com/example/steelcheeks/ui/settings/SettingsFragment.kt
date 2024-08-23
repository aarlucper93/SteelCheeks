package com.example.steelcheeks.ui.settings

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.steelcheeks.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themePreference: ListPreference? = findPreference("theme")
        themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener{ _, newValue ->
            applyTheme(newValue.toString())
            true
        }
    }

    private fun applyTheme(themePreference: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = requireContext().getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

            when (themePreference) {
                "Use system default" -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
                "Light" -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
                "Dark" -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            }
        }
        else {
            when (themePreference) {
                "Use system default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }


    }

}