package com.V2Skydivejump.app.utils

import kotlinx.coroutines.delay

object WeatherUtils {
    /**
     * Placeholder for a future Ktor API call to fetch weather based on date and location.
     */
    suspend fun fetchWeather(date: Long, location: String): String {
        // Simulate network delay
        delay(1000)
        return "Clear Skies, 15°C, 5kts Wind"
    }
}
