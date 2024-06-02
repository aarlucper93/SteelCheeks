package com.example.steelcheeks.ui.food

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.steelcheeks.data.FoodRepository
import com.example.steelcheeks.data.database.food.FoodEntity
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.example.steelcheeks.data.network.OpenFoodFactsResponse
import com.example.steelcheeks.domain.Food
import com.example.steelcheeks.utils.SingleLiveEvent
import kotlinx.coroutines.launch

enum class LoadingStatus { READY, LOADING, ERROR, DONE }

class FoodsViewModel(private val repository: FoodRepository) : ViewModel() {

    val localFoodList: LiveData<List<FoodEntity>> = repository.getLocalFoodList().asLiveData()

    //private val _foodItems = MutableLiveData<List<Food>>(localFoodList.value?.map { repository.toDomainModel(it) })
    private val _foodItems = MutableLiveData<List<Food>>(listOf())
    val foodItems: LiveData<List<Food>> = _foodItems

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    init {
        _searchQuery.value = ""
    }

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
                    Log.d("response", "All good")
                    Log.d("response", "$response")
                    //TODO: Handle response
                    val product = response.body()?.product
                    if (product != null) {
                        _food.value = repository.toDomainModel(product)
                        Log.d("Mapped food: ", "${food.value}")
                        _itemWasScanned.value = true
                        callback.invoke(barcode)
                        _status.value = LoadingStatus.DONE
                    } else {
                        _status.value = LoadingStatus.ERROR
                    }
                } else {
                    Log.d("response", "$response")
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
            _foodItems.value = localFoodList.value?.map {repository.toDomainModel(it)}
            Log.d("FoodsViewModel", "${_foodItems.value}")
        }
    }

    fun filterLocalFoodList(query: String) {
        if (query.isBlank()) {
            _filteredLocalFoodList.value = localFoodList.value
        } else {
            viewModelScope.launch {
                val filteredList = localFoodList.value?.filter {
                    it.productName.contains(query, true) || it.productBrands!!.contains(query, true)        //TODO: Control for nullable productBrands
                } ?: emptyList()
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

    fun setLoadingStatusAsReady() {
        _status.value = LoadingStatus.READY
    }

    fun setRemoteListMode(isRemoteList: Boolean) {
        _remoteListMode.value = isRemoteList
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