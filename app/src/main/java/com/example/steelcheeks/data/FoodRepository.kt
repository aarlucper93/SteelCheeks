package com.example.steelcheeks.data

import android.util.Log
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.data.database.diary.DiaryTotals
import com.example.steelcheeks.data.database.food.FoodEntity
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.example.steelcheeks.data.network.Product
import com.example.steelcheeks.data.network.OpenFoodFactsResponse
import com.example.steelcheeks.data.network.FoodsApi
import com.example.steelcheeks.domain.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import org.threeten.bp.LocalDate

class FoodRepository(private val database: FoodRoomDatabase) {

    private var result = -1L
    suspend fun insertFood(food: Food): Long {
        val foodEntity = FoodEntity(
            food.code,
            food.productBrands ?: "No brand specified",
            food.productName,
            food.imageUrl,
            food.productQuantity,
            food.productQuantityUnit,
            food.energyKcal,
            food.carbohydrates,
            food.proteins,
            food.fat
        )
        withContext(Dispatchers.IO) {
            result = database.foodDao().insert(foodEntity)
        }

        return result
    }

    suspend fun getFoodList(searchTerms: String): Response<OpenFoodFactsResponse>? {
        return withContext(Dispatchers.IO) {
            try {
                FoodsApi.retrofitService.getFoodList(searchTerms)
            } catch (e: Exception) {
                Log.e("FoodRepository", "Error fetching data: ${e.message}", e)
                null
            }
        }
    }

    fun getLocalFoodList(): Flow<List<FoodEntity>> = database.foodDao().getAllFoods()

    suspend fun insertDiaryEntry(diaryEntry: DiaryEntryEntity): Long {
        return withContext(Dispatchers.IO) {
            database.diaryDao().insert(diaryEntry)
        }
    }

    fun getDiaryEntriesForDate(date: LocalDate): Flow<List<DiaryEntryEntity>> {
        return database.diaryDao().getDiaryEntriesForDate(date)
    }

    fun getDiaryTotalsForDate(date: LocalDate): Flow<DiaryTotals> {
        return database.diaryDao().getDiaryTotalsForDate(date)
    }

    fun toDomainModel(product: Product): Food {
        return Food(
            code = product.code,
            productName = product.productName,
            productBrands = product.productBrands,
            imageUrl = product.imageUrl,
            productQuantityUnit = product.productQuantityUnit,
            energyKcal = product.nutriments.energyKcal,
            carbohydrates = product.nutriments.carbohydrates,
            proteins = product.nutriments.proteins,
            fat = product.nutriments.fat,
            isFromLocal = false
        )
    }

    fun toDomainModel(foodEntity: FoodEntity): Food {
        return Food(
            code = foodEntity.code,
            productName = foodEntity.productName,
            productBrands = foodEntity.productBrands,
            imageUrl = foodEntity.imageUrl,
            productQuantityUnit = foodEntity.productQuantityUnit,
            energyKcal = foodEntity.energyKcal,
            carbohydrates = foodEntity.carbohydrates,
            proteins = foodEntity.proteins,
            fat = foodEntity.fat,
            isFromLocal = true
        )
    }




}