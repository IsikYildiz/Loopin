package com.example.loopin.network

import com.example.loopin.network.Api.OpenMeteoApi;
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object OpenMeteoApiClient {
    private const val BASE_URL = "https://api.open-meteo.com/"
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Veya GsonConverterFactory
            .build()
    }

    val weatherApi: OpenMeteoApi by lazy {
        retrofit.create(OpenMeteoApi::class.java)
    }
}
