package com.example.steelcheeks.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET


//TODO: Cambiar url por https://world.openfoodfacts.org/cgi/search.pl?search_terms=natillas hacendado&search_simple=1&action=process&json=1&fields=product_name,brands,nutriments
//Siendo search_terms el criterio de busqueda
const val BASE_URL = "https://world.openfoodfacts.org/cgi/"
const val QUERY_PARAMETERS = "search.pl?search_terms=proteínas hacendado&search_simple=1&action=process&json=1&fields=product_name,brands,nutriments,code"


//Build Moshi object that Retrofit will be using
private val moshi = Moshi.Builder()
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
    .addInterceptor(UserAgentInterceptor("ButtcheeksReloaded/0.1.0 (aaronlp.ai93@gmail.com)"))
    .build()

//Build Retrofit object using Moshi converter
val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(okHttpClient) // Set the OkHttpClient with the User-Agent interceptor
    .build()

//Returns a single Food entry TODO: change to a list of foods
interface FoodsApiService {
    @GET(QUERY_PARAMETERS)
    suspend fun getFoodList() : retrofit2.Response<FoodList>
}

//Public Api object that exposes the lazy-initialized Retrofit service
object FoodsApi {
    val retrofitService: FoodsApiService by lazy {
        retrofit.create(FoodsApiService::class.java)
    }
}