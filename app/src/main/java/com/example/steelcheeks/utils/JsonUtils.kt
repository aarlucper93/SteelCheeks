package com.example.steelcheeks.utils

import com.example.steelcheeks.data.database.food.FoodEntity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object JsonUtils {

    suspend fun convertFoodEntitiesToJson(foods: List<FoodEntity>) : String =
        withContext(Dispatchers.Default) {
            val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val jsonAdapter : JsonAdapter<List<FoodEntity>> =
                moshi.adapter(Types.newParameterizedType(List::class.java, FoodEntity::class.java))
            jsonAdapter.toJson(foods)
        }

}