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
    @Json(name = "product_name") val productName: String = "Not provided",
    @Json(name = "brands") val productBrands: String? = "Not provided",
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "product_quantity") val productQuantity : Long = 100,
    @Json(name = "product_quantity_unit") val productQuantityUnit : String = "g",
    val nutriments: Nutriments
)

data class Nutriments(
    @Json(name = "energy-kcal") val energyKcal: Double? = 0.0,
    @Json(name = "carbohydrates") val carbohydrates: Double? = 0.0,
    @Json(name = "proteins") val proteins: Double? = 0.0,
    @Json(name = "fat") val fat: Double? = 0.0
)

