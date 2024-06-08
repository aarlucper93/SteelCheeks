package com.example.steelcheeks.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://world.openfoodfacts.org/cgi/"

//TODO: Extract common network tools into own file

//Build Moshi object that Retrofit will be using
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Create an Interceptor to set the custom User-Agent header
class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val requestWithUserAgent: Request = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }
}

// Create an OkHttpClient and add the UserAgentInterceptor
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(UserAgentInterceptor("SteelCheeks/0.1.0 (aaronlp.ai93@gmail.com)"))
    .connectTimeout(45, java.util.concurrent.TimeUnit.SECONDS) // Connection timeout
    .readTimeout(45, java.util.concurrent.TimeUnit.SECONDS)    // Read timeout
    .writeTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
    .callTimeout(60, java.util.concurrent.TimeUnit.SECONDS)

    .build()

//Build Retrofit object using Moshi converter
val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(okHttpClient) // Set the OkHttpClient with the User-Agent interceptor
    .build()

//Returns a list of foods
interface FoodSearchApiService {
    @GET("search.pl")
    suspend fun getFoodList(
        @Query("search_terms") searchTerms: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("fields") fields: String = "product_name,brands,nutriments,code,image_url,serving_quantity,product_quantity_unit"
    ): retrofit2.Response<OpenFoodFactsResponse>
}

//Public Api object that exposes the lazy-initialized Retrofit service
object FoodSearchApi {
    val retrofitService: FoodSearchApiService by lazy {
        retrofit.create(FoodSearchApiService::class.java)
    }
}