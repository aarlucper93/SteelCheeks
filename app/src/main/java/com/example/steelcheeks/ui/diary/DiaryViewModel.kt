package com.example.steelcheeks.ui.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.steelcheeks.data.FoodRepository
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.data.database.diary.DiaryTotals
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class DiaryViewModel (private val repository: FoodRepository) : ViewModel() {


    private val _date = MutableLiveData<LocalDate>(LocalDate.now())
    val date: LiveData<LocalDate> get() = _date

    val diaryEntries: LiveData<List<DiaryEntryEntity>> = _date.switchMap() {
        repository.getDiaryEntriesForDate(it).asLiveData()
    }

    val diaryTotals: LiveData<DiaryTotals> = _date.switchMap() {
        repository.getDiaryTotalsForDate(it).asLiveData()
    }

    fun insertDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.insertDiaryEntry(entry)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.deleteDiaryEntry(entry)
            // Update the totals and entries after deletion
            _date.value = _date.value
        }
    }

    fun setDate(newDate: LocalDate) {
        _date.value = newDate
    }
}

class DiaryViewModelFactory(private val database: FoodRoomDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            val repository = FoodRepository(database)
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}