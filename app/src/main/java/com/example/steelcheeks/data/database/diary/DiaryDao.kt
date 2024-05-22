package com.example.steelcheeks.data.database.diary

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

@Dao
interface DiaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diaryEntryEntity: DiaryEntryEntity) : Long

    @Update
    suspend fun update(diaryEntryEntity: DiaryEntryEntity)

    @Delete
    suspend fun delete(diaryEntryEntity: DiaryEntryEntity)

    @Query("SELECT * FROM diary WHERE date = :date ORDER BY :date")
    fun getDiaryEntriesForDate(date: LocalDate) : Flow<List<DiaryEntryEntity>>

    @Query("""
        SELECT 
            SUM(f.`energy-kcal` * d.serving_quantity / 100) AS totalCalories,
            SUM(f.carbohydrates * d.serving_quantity / 100) AS totalCarbohydrates,
            SUM(f.proteins * d.serving_quantity / 100) AS totalProteins,
            SUM(f.fat * d.serving_quantity / 100) AS totalFat
        FROM diary d
        JOIN food f ON d.food_code = f.code
        WHERE d.date = :date
    """)
    fun getDiaryTotalsForDate(date: LocalDate) : Flow<DiaryTotals>
}

data class DiaryTotals(
    val totalCalories: Double?,
    val totalCarbohydrates: Double?,
    val totalProteins: Double?,
    val totalFat: Double?
)