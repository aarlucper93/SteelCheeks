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
    @ColumnInfo(name = "serving_quantity") val quantity: Long = 100,
    @ColumnInfo(name = "product_quantity_unit") val productQuantityUnit : String? = "g",
    @ColumnInfo(name = "product_name") val productName: String,
    @ColumnInfo(name = "brands") val productBrands: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "energy-kcal") val energyKcal: Double?,
    @ColumnInfo(name = "carbohydrates") val carbohydrates: Double?,
    @ColumnInfo(name = "proteins") val proteins: Double?,
    @ColumnInfo(name = "fat") val fat: Double?

    )
