package com.example.steelcheeks.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import retrofit2.http.Url

@Entity (tableName = "food")
data class FoodEntity (
    @PrimaryKey
    val code: String,
    @ColumnInfo(name = "product_name") val productName: String,
    @ColumnInfo(name = "brands") val productBrands: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "energy-kcal") val energyKcal: Double?,
    @ColumnInfo(name = "carbohydrates") val carbohydrates: Double?,
    @ColumnInfo(name = "proteins") val proteins: Double?,
    @ColumnInfo(name = "fat") val fat: Double?
    )