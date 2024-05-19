package com.example.steelcheeks.data.database.diary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "diary")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "food_code") val foodCode: String,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "quantity") val quantity: Long,

    )
