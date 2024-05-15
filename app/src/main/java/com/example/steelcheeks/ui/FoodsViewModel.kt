package com.example.steelcheeks.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.steelcheeks.data.FoodRepository
import com.example.steelcheeks.data.database.FoodEntity
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.example.steelcheeks.data.network.Food
import com.example.steelcheeks.data.network.FoodList
import com.example.steelcheeks.data.network.Nutriments
import kotlinx.coroutines.launch

enum class FoodsApiStatus { LOADING, ERROR, DONE }

class FoodsViewModel(private val repository: FoodRepository) : ViewModel() {

    val localFoodList: LiveData<List<FoodEntity>> =
        repository.getLocalFoodList().asLiveData()

    private val _filteredLocalFoodList = MutableLiveData<List<FoodEntity>>()
    val filteredLocalFoodList: LiveData<List<FoodEntity>> = _filteredLocalFoodList

    //Status of the most recent request
    private val _status = MutableLiveData<FoodsApiStatus>()
    val status: LiveData<FoodsApiStatus> = _status

    //List of products returned by the request
    private val _products = MutableLiveData<FoodList>()
    val products: LiveData<FoodList> = _products

    //The Food returned by the request -- OLD
    private val _food = MutableLiveData<Food>(null)
    val food: LiveData<Food> = _food

    private val _result = MutableLiveData<Long>(-1L)
    val result: LiveData<Long> = _result

    var isLocalLoad: Boolean = false

    //Nutriments of the food returned by the request
    /*val kcal: LiveData<Double?> = food.map { it.product.nutriments.energyKcal }
    val carbohydrates: LiveData<Double?> = food.map {it.product.nutriments.carbohydrates }
    val proteins: LiveData<Double?> = food.map { it.product.nutriments.proteins }*/

    //Launches a coroutine that uses the Foods Api Retrofit Service to get a Food entry
    fun getFoodEntries(searchTerms: String) {
        viewModelScope.launch {
            _status.value = FoodsApiStatus.LOADING
            try {
                val response = repository.getFoodList(searchTerms)
                response?.let {
                    if (response.isSuccessful) {
                        _products.value = response.body()
                        _products.value?.let {
                            if (it.count > 0) {                 // Products found
                                _status.value = FoodsApiStatus.DONE
                            } else {                            // No products found
                                _status.value = FoodsApiStatus.ERROR
                            }
                        }
                    }
                }
                if (response == null) {
                    throw Exception("Response returned null")
                }
            } catch (e: Exception) {
                _status.value = FoodsApiStatus.ERROR
                Log.e("FoodsViewModel", "Error fetching data: ${e.message}", e)
            }
        }
    }

    fun filterLocalFoodList(query: String) {
        if (query.isBlank()) {
            _filteredLocalFoodList.value = localFoodList.value
        } else {
            val filteredList = localFoodList.value?.filter {
                it.productName.contains(query, true) || it.productBrands!!.contains(query, true)        //TODO: Control for nullable productBrands
            } ?: emptyList()
            _filteredLocalFoodList.value = filteredList
        }
    }

    fun setFoodItemByBarcode(barcode: String) {
        if (isLocalLoad) {
            //Map selected local food item to Food Livedata
            val foodEntity = localFoodList.value?.filter { it.code == barcode }?.get(0)
            foodEntity?.let {
                _food.value = Food (
                    code = foodEntity.code,
                    productName = foodEntity.productName,
                    productBrands = foodEntity.productBrands,
                    imageUrl = foodEntity.imageUrl,
                    productQuantityUnit = foodEntity.productQuantityUnit,
                    nutriments = Nutriments(
                        energyKcal = foodEntity.energyKcal,
                        carbohydrates = foodEntity.carbohydrates,
                        proteins = foodEntity.proteins,
                        fat = foodEntity.fat
                    )
                )
            }
        }
        else {
            _food.value = _products.value?.products?.filter { it.code == barcode }?.get(0)
        }
    }

    fun insertFoodToLocalDatabase(food: LiveData<Food>) {
        viewModelScope.launch {
            _result.value = repository.insertFood(food.value!!)
        }
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