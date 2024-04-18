package com.example.steelcheeks.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.steelcheeks.network.Food
import com.example.steelcheeks.network.FoodList
import com.example.steelcheeks.network.FoodsApi
import kotlinx.coroutines.launch

enum class FoodsApiStatus { LOADING, ERROR, DONE }

class FoodsViewModel : ViewModel() {

    //Status of the most recent request
    private val _status = MutableLiveData<FoodsApiStatus>()
    val status: LiveData<FoodsApiStatus> = _status

    //List of products returned by the request
    private val _products = MutableLiveData<FoodList>()
    val products: LiveData<FoodList> = _products

    //The Food returned by the request -- OLD
    private val _food = MutableLiveData<Food>()
    val food: LiveData<Food> = _food

    //Nutriments of the food returned by the request
    /*val kcal: LiveData<Double?> = food.map { it.product.nutriments.energyKcal }
    val carbohydrates: LiveData<Double?> = food.map {it.product.nutriments.carbohydrates }
    val proteins: LiveData<Double?> = food.map { it.product.nutriments.proteins }*/

    //Launches a coroutine that uses the Foods Api Retrofit Service to get a Food entry
    fun getFoodEntries(searchTerms: String) {
        viewModelScope.launch {
            _status.value = FoodsApiStatus.LOADING
            try {
                val response = FoodsApi.retrofitService.getFoodList(searchTerms = searchTerms)
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
            } catch (e: Exception) {
                _status.value = FoodsApiStatus.ERROR
            }
        }
    }
}