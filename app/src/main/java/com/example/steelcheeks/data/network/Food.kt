package com.example.steelcheeks.data.network

import com.squareup.moshi.Json

data class FoodList (
    val products: List<Food>,
    val count: Int
)

data class Food (
    val code: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "brands") val productBrands: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "product_quantity") val productQuantity : Long = 100,
    @Json(name = "product_quantity_unit") val productQuantityUnit : String = "g",
    val nutriments: Nutriments
)

data class Nutriments(
    @Json(name = "energy-kcal") val energyKcal: Double?,
    @Json(name = "carbohydrates") val carbohydrates: Double?,
    @Json(name = "proteins") val proteins: Double?,
    @Json(name = "fat") val fat: Double?
    //TODO: Get serving_size and product_quantity_unit
    /* Add later

      @Json(name = "saturated-fat") val saturatedFat: Double,
      @Json(name = "salt") val salt: Double */
)