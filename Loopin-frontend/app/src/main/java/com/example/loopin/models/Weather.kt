package com.example.loopin.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// API yanıtının ana yapısı
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "latitude")
    val latitude: Double?,
    @Json(name = "longitude")
    val longitude: Double?,
    @Json(name = "generationtime_ms")
    val generationtimeMs: Double?,
    @Json(name = "utc_offset_seconds")
    val utcOffsetSeconds: Int?,
    @Json(name = "timezone")
    val timezone: String?,
    @Json(name = "timezone_abbreviation")
    val timezoneAbbreviation: String?,
    @Json(name = "elevation")
    val elevation: Double?,
    @Json(name = "current_weather")
    val currentWeather: CurrentWeather?,
    @Json(name = "daily") // Günlük tahminler için
    val daily: DailyForecasts?
)

// Anlık hava durumu bilgileri
@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "temperature")
    val temperature: Double?,
    @Json(name = "windspeed")
    val windspeed: Double?,
    @Json(name = "winddirection")
    val winddirection: Double?,
    @Json(name = "weathercode")
    val weathercode: Int?, // Hava durumu kodu (ikon eşleştirmesi için)
    @Json(name = "is_day")
    val isDay: Int?, // 1 gündüz, 0 gece
    @Json(name = "time")
    val time: String?
)

// Günlük tahmin verileri (Open-Meteo'da genellikle array'ler halinde gelir)
@JsonClass(generateAdapter = true)
data class DailyForecasts(
    @Json(name = "time")
    val time: List<String>?, // Tarih listesi
    @Json(name = "weathercode")
    val weathercode: List<Int>?, // Her gün için hava durumu kodu
    @Json(name = "temperature_2m_max")
    val temperature2mMax: List<Double>?, // Her gün için maksimum sıcaklık
    @Json(name = "temperature_2m_min")
    val temperature2mMin: List<Double>?,  // Her gün için minimum sıcaklık
    @Json(name = "precipitation_sum")
    val precipitationSum: List<Double>? // Yağış miktarı
)