package com.example.steelcheeks.domain

data class Food (
    val code: String,
    val productName: String,
    val productBrands: String?,
    val imageUrl: String?,
    val productQuantity : Long = 100,
    val productQuantityUnit : String = "g",
    val energyKcal: Double?,
    val carbohydrates: Double?,
    val proteins: Double?,
    val fat: Double?,
    val isFromLocal: Boolean
)




