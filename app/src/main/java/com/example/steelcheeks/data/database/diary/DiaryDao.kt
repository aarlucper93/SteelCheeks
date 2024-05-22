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
            SUM(`energy-kcal` * serving_quantity / 100) AS totalCalories,
            SUM(carbohydrates * serving_quantity / 100) AS totalCarbohydrates,
            SUM(proteins * serving_quantity / 100) AS totalProteins,
            SUM(fat * serving_quantity / 100) AS totalFat
        FROM diary
        WHERE date = :date
    """)
    fun getDiaryTotalsForDate(date: LocalDate) : Flow<DiaryTotals>
}

data class DiaryTotals(
    val totalCalories: Double?,
    val totalCarbohydrates: Double?,
    val totalProteins: Double?,
    val totalFat: Double?
)