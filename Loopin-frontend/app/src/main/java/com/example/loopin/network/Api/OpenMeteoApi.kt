package com.example.loopin.network.Api

import com.example.loopin.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast") // Open-Meteo'nun temel forecast endpoint'i

    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("timezone") timezone: String = "auto", // Zaman dilimini otomatik algılasın
        @Query("daily") daily: String? = "weathercode,temperature_2m_max,temperature_2m_min",
        @Query("precipitation_sum") precipitationSum: String? = "precipitation_sum",
        @Query("start_date") startDate: String, // YYYY-MM-DD formatında
        @Query("end_date") endDate: String,     // YYYY-MM-DD formatında
        @Query("temperature_unit") tempUnit: String = "celsius"
    ): Response<WeatherResponse>
}