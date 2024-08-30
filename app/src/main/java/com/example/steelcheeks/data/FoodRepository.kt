package com.example.steelcheeks.data

import android.util.Log
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.data.database.diary.DiaryTotals
import com.example.steelcheeks.data.database.food.FoodEntity
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.example.steelcheeks.data.network.Product
import com.example.steelcheeks.data.network.OpenFoodFactsResponse
import com.example.steelcheeks.data.network.FoodSearchApi
import com.example.steelcheeks.data.network.SingleFoodApi
import com.example.steelcheeks.data.network.SingleProductOffResponse
import com.example.steelcheeks.domain.Food
import com.example.steelcheeks.utils.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Response
import org.threeten.bp.LocalDate

class FoodRepository(private val database: FoodRoomDatabase) {

    private var result = -1L
    suspend fun insertFood(food: Food): Long {
        val foodEntity = FoodEntity(
            food.code,
            food.productName ?: "No name specified",
            food.productBrands ?: "No brand specified",
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

    private lateinit var resultList : List<Long>
    suspend fun insertFoodList(foodList: List<Food>): List<Long> {
        val foodListEntity = foodList.map {toEntityModel(it)
        }
        withContext(Dispatchers.IO) {
            resultList = database.foodDao().insertAll(foodListEntity)
        }

        return resultList
    }

    suspend fun insertFoodListFromJson(jsonString: String): List<Long> {

        var foodList : List<FoodEntity>?
        withContext(Dispatchers.Main) {
            foodList = JsonUtils.convertJsonToFoodEntities(jsonString)
        }
        withContext(Dispatchers.IO) {
            foodList?.let {
                resultList = database.foodDao().insertAll(it)
            }
        }
        return resultList
    }

    suspend fun getFoodList(searchTerms: String): Response<OpenFoodFactsResponse>? {
        return withContext(Dispatchers.IO) {
            try {
                FoodSearchApi.retrofitService.getFoodList(searchTerms)
            } catch (e: Exception) {
                Log.e("FoodRepository", "Error fetching data: ${e.message}", e)
                null
            }
        }
    }

    suspend fun getProductByBarcode(barcode: String): Response<SingleProductOffResponse>? {
        return withContext(Dispatchers.IO) {
            try {
                SingleFoodApi.retrofitService.getProductByBarcode(barcode)
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

    suspend fun deleteDiaryEntry(diaryEntry: DiaryEntryEntity) {
        withContext(Dispatchers.IO) {
            database.diaryDao().delete(diaryEntry)
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

    fun toEntityModel(food: Food) : FoodEntity{
        return FoodEntity(
            code = food.code,
            productName = food.productName,
            productBrands = food.productBrands,
            imageUrl = food.imageUrl,
            productQuantityUnit = food.productQuantityUnit,
            energyKcal = food.energyKcal,
            carbohydrates = food.carbohydrates,
            proteins = food.proteins,
            fat = food.fat,
        )
    }

    suspend fun convertFoodEntitiesToJson() : String =
        withContext(Dispatchers.IO) {
            val foods = getLocalFoodList().first()
            JsonUtils.convertFoodEntitiesToJson(foods)
        }

}