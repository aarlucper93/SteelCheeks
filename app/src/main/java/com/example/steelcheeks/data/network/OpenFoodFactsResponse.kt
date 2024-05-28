package com.example.steelcheeks.data.network

import com.squareup.moshi.Json

data class OpenFoodFactsResponse (
    val products: List<Product>,
    val count: Int
)

data class SingleProductOffResponse (
    val product: Product
)

data class Product (
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
)

