package com.example.steelcheeks.data

import android.util.Log
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.data.database.diary.DiaryTotals
import com.example.steelcheeks.data.database.food.FoodEntity
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.example.steelcheeks.data.network.Food
import com.example.steelcheeks.data.network.FoodList
import com.example.steelcheeks.data.network.FoodsApi
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
            food.productName,
            food.productBrands,
            food.imageUrl,
            food.productQuantity,
            food.productQuantityUnit,
            food.nutriments.energyKcal,
            food.nutriments.carbohydrates,
            food.nutriments.proteins,
            food.nutriments.fat
        )
        withContext(Dispatchers.IO) {
            result = database.foodDao().insert(foodEntity)
        }

        return result
    }

    suspend fun getFoodList(searchTerms: String): Response<FoodList>? {
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


}