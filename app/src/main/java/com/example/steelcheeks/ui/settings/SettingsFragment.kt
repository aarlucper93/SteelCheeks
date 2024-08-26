package com.example.steelcheeks.ui.settings

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.steelcheeks.R
import com.example.steelcheeks.SteelCheeksApplication

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            (activity?.application as SteelCheeksApplication).database
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themePreference: ListPreference? = findPreference("theme")
        themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener{ _, newValue ->
            applyTheme(newValue.toString())
            true
        }

        val exportPreference: Preference? = findPreference("backup_database")
        exportPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->
            viewModel.getDatabaseAsJsonString()
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

    private fun displayJsonString (jsonString: String) {
        Log.d("SettingsFragment", jsonString)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.jsonString.observe(viewLifecycleOwner) { jsonString ->
            displayJsonString(jsonString)
        }
    }
}