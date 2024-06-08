package com.example.steelcheeks.data.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val PRODUCT_BASE_URL = "https://world.openfoodfacts.net/api/v2/"

//Build Retrofit object using Moshi converter
val productRetrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(PRODUCT_BASE_URL)
    .client(okHttpClient) // Set the OkHttpClient with the User-Agent interceptor
    .build()

//Returns a list of foods
interface SingleFoodApiService {
    @GET("product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "product_name,brands,nutriments,code,image_url,serving_quantity,product_quantity_unit"
    ): retrofit2.Response<SingleProductOffResponse>
}



//Public Api object that exposes the lazy-initialized Retrofit service
object SingleFoodApi {
    val retrofitService: SingleFoodApiService by lazy {
        productRetrofit.create(SingleFoodApiService::class.java)
    }
}
