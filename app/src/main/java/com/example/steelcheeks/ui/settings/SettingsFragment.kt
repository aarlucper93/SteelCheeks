package com.example.steelcheeks.ui.settings

import android.app.UiModeManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.steelcheeks.R
import com.example.steelcheeks.SteelCheeksApplication
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            (activity?.application as SteelCheeksApplication).database
        )
    }

    private val createFileLauncher = registerForActivityResult(
        CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.jsonString.value?.let {
                writeToFile(uri, it)
            }
        }
    }

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val jsonString = readFileContents(uri)
            jsonString?.let {
                viewModel.populateDatabaseFromJson(jsonString)
            }
        }
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
            viewModel.getDatabaseAsJsonString()     //Obtain JSON string
            createFile()                            //Launch intent to create file
            true
        }

        val importPreference: Preference? = findPreference("import_database")
        importPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->
            importFromFile()
            true
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.jsonString.observe(viewLifecycleOwner) { jsonString ->
            displayJsonString(jsonString)
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) {message ->
            showSnackbar(message)

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

    private fun createFile() {
        createFileLauncher.launch("database_backup.json")
    }

    private fun writeToFile(uri: Uri, jsonString: String) {
        try {
            context?.contentResolver?.openOutputStream(uri)?.use {   //Ensures the resource is closed even if an exception is thrown
                it.write(jsonString.toByteArray())
                it.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SettingsFragment", "Error writing file", e)
            showSnackbar("Failed to create file")
        }
    }

    //Get the contents of the file
    private fun readFileContents(uri: Uri): String? {
        return try {
            context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SettingsFragment", "Error reading file", e)
            null
        }
    }

    private fun importFromFile() {
        pickFileLauncher.launch(arrayOf("application/json"))
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}