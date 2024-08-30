package com.example.steelcheeks.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.steelcheeks.data.FoodRepository
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.example.steelcheeks.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _jsonString = MutableLiveData<String>()
    val jsonString: LiveData<String> get() = _jsonString

    private val _snackbarMessage = SingleLiveEvent<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    fun getDatabaseAsJsonString() {
        viewModelScope.launch {
            val json = repository.convertFoodEntitiesToJson()
            _jsonString.value = json
        }
    }

    fun populateDatabaseFromJson(jsonString: String) {
        viewModelScope.launch {
            val result = repository.insertFoodListFromJson(jsonString)
            if (result.contains(-1L)) {
                _snackbarMessage.value = "An error occurred"
            } else {
                _snackbarMessage.value = "${result.size} food items imported"
            }

        }
    }
}

class SettingsViewModelFactory(private val database: FoodRoomDatabase) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            // Initialize the repository
            val repository = FoodRepository(database)
            @Suppress("UNCHECKED_CAST")
            return  SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}