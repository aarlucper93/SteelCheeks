package com.example.steelcheeks.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodEntities: List<FoodEntity>)

    @Update
    suspend fun update(foodEntity: FoodEntity)

    @Delete
    suspend fun delete(foodEntity: FoodEntity)

    @Query("SELECT * FROM food WHERE code = :code")
    fun getFood(code: String): Flow<FoodEntity>

    @Query("SELECT * FROM food ORDER BY code")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT COUNT(*) FROM food")
    fun getFoodCount(): Flow<Int>
}