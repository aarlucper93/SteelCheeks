package com.example.steelcheeks.ui.food

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.steelcheeks.data.FoodRepository
import com.example.steelcheeks.data.database.food.FoodEntity
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.data.network.OpenFoodFactsResponse
import com.example.steelcheeks.domain.Food
import com.example.steelcheeks.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

enum class LoadingStatus { READY, LOADING, ERROR, DONE }

class FoodsViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _foodItems = MutableLiveData<List<Food>>(listOf())
    val foodItems: LiveData<List<Food>> = _foodItems

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _filteredLocalFoodList = MutableLiveData<List<FoodEntity>>()
    val filteredLocalFoodList: LiveData<List<FoodEntity>> = _filteredLocalFoodList

    //Status of the most recent request
    private val _status = MutableLiveData<LoadingStatus>()
    val status: LiveData<LoadingStatus> = _status

    //List of products returned by the request
    private val _products = MutableLiveData<OpenFoodFactsResponse>()
    val products: LiveData<OpenFoodFactsResponse> = _products

    //The Food returned by the request -- OLD
    private val _food = MutableLiveData<Food>(null)
    val food: LiveData<Food> = _food

    private val _result = MutableLiveData<Long>(-1L)
    val result: LiveData<Long> = _result

    private val _snackbarMessage = SingleLiveEvent<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _remoteListMode = MutableLiveData<Boolean>(false)
    val remoteListMode: LiveData<Boolean> = _remoteListMode

    private val _itemWasScanned = MutableLiveData<Boolean>(false)
    val itemWasScanned: LiveData<Boolean> = _itemWasScanned

    private val _selectedItems = MutableLiveData<List<Food>>(listOf())
    val selectedItems: LiveData<List<Food>> get() = _selectedItems

    private val _selectedCount = MutableLiveData<Int>(0)
    val selectedCount: LiveData<Int> get() = _selectedCount

    private val _newEntryDate = MutableLiveData<LocalDate>(null)
    val newEntryDate = _newEntryDate


    init {
        fetchLocalFoodItems()
        _searchQuery.value = ""     //TODO: List doesn't appear filtered after having added an item to the diary

    }


    //Nutriments of the food returned by the request
    /*val kcal: LiveData<Double?> = food.map { it.product.nutriments.energyKcal }
    val carbohydrates: LiveData<Double?> = food.map {it.product.nutriments.carbohydrates }
    val proteins: LiveData<Double?> = food.map { it.product.nutriments.proteins }*/

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    //Launches a coroutine that uses the Foods Api Retrofit Service to get a Food entry
    fun getFoodEntries(searchTerms: String) {
        viewModelScope.launch {
            _status.value = LoadingStatus.LOADING
            try {
                val response = repository.getFoodList(searchTerms)
                response?.let {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let {
                            if (it.count > 0) {                 // Products found
                                _itemWasScanned.value = false
                                _foodItems.value = it.products.map { product -> repository.toDomainModel(product) }
                                _status.value = LoadingStatus.DONE
                            } else {                            // No products found
                                _status.value = LoadingStatus.ERROR
                            }
                        }
                    }
                }
                if (response == null) {
                    throw Exception("Response returned null")
                }
            } catch (e: Exception) {
                _status.value = LoadingStatus.ERROR
                Log.e("FoodsViewModel", "Error fetching data: ${e.message}", e)
            }
        }
    }

    fun getFoodByBarcode(barcode: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            _status.value = LoadingStatus.LOADING
            try {
                val response = repository.getProductByBarcode(barcode)
                if (response?.isSuccessful == true) {
                    val product = response.body()?.product
                    if (product != null) {
                        _food.value = repository.toDomainModel(product)
                        _itemWasScanned.value = true
                        callback.invoke(barcode)
                        _status.value = LoadingStatus.DONE
                    } else {
                        _status.value = LoadingStatus.ERROR
                    }
                } else {
                    _status.value = LoadingStatus.ERROR
                }
            } catch (e: Exception) {
                _status.value = LoadingStatus.ERROR
                Log.e("FoodsViewModel", "Error fetching data: ${e.message}", e)
            }
        }
    }

    fun setListToLocalFoodItems() {
        viewModelScope.launch {
            val foodEntities = withContext(Dispatchers.IO) {
                repository.getLocalFoodList().first()
            }
            _foodItems.value = foodEntities.map { repository.toDomainModel(it) }
        }
    }

    private fun fetchLocalFoodItems() {
        viewModelScope.launch {
            val foodEntities = repository.getLocalFoodList().first()
            _foodItems.value = foodEntities.map { repository.toDomainModel(it) }
        }
    }

    fun filterLocalFoodList(query: String) {
        if (query.isBlank()) {
            fetchLocalFoodItems()
        } else {
            viewModelScope.launch {
                val filteredList = withContext(Dispatchers.IO) {
                    repository.getLocalFoodList().first().filter {
                        it.productName.contains(query, true) || it.productBrands!!.contains(
                            query,
                            true
                        )
                    }
                }
                _foodItems.value = filteredList.map { repository.toDomainModel(it) }
            }
        }
    }

    fun setFoodItemByBarcode(barcode: String) {
        _food.value = _foodItems.value?.filter { it.code == barcode }?.get(0)
    }

    fun setScannedItemAsLoaded() {
        _itemWasScanned.value = false
    }

    fun insertFoodToLocalDatabase(food: LiveData<Food>) {
        viewModelScope.launch {
            _result.value = repository.insertFood(food.value!!)
            if (result.value != -1L) {
                _snackbarMessage.value = "Food saved to the local database"
            }
        }
    }

    fun insertFoodListToLocalDatabase() {
        viewModelScope.launch {
            val result = _selectedItems.value?.let { repository.insertFoodList(it) }
            result?.let {
                if (result.contains(-1L)) {
                    _snackbarMessage.value = "An error occurred"
                } else {
                    _snackbarMessage.value = "Foods saved to the local database"
                }
            }

        }
    }

    fun insertDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.insertDiaryEntry(entry)
        }
    }

    fun setLoadingStatusAsReady() {
        _status.value = LoadingStatus.READY
    }

    fun setRemoteListMode(isRemoteList: Boolean) {
        _remoteListMode.value = isRemoteList
    }

    fun toggleItemSelected(food: Food, isSelected: Boolean) {
        food.isSelected = isSelected
        val currentList = _selectedItems.value?.toMutableList() ?: mutableListOf()
        if (isSelected) {
            currentList.add(food)
        } else {
            currentList.remove(food)
        }
        _selectedItems.value = currentList
        _selectedCount.value = currentList.size
    }

    fun clearSelectedItems() {
        _selectedItems.value = listOf()
        _selectedCount.value = 0
        _foodItems.value = _foodItems.value?.map { it.copy(isSelected = false) }  //Clear checkboxes
    }

    fun setDateForNewEntry(selectedDate : Long?)  {
        _newEntryDate.value = LocalDate.ofEpochDay(selectedDate ?: LocalDate.now().toEpochDay())
    }

    fun getDateForNewEntry() : LocalDate {
        return _newEntryDate.value ?: LocalDate.now()
    }

}

//Allows passing the database as a parameter when initializing the viewModel.
class FoodsViewModelFactory(private val database: FoodRoomDatabase) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodsViewModel::class.java)) {
            // Initialize the repository
            val repository = FoodRepository(database)
            @Suppress("UNCHECKED_CAST")
            return  FoodsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}