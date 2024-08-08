package com.example.steelcheeks.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.steelcheeks.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}