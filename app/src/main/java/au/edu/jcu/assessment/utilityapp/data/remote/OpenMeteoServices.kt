package au.edu.jcu.assessment.utilityapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Open-Meteo Air Quality API (free, no API key needed).
 * Docs: https://open-meteo.com/en/docs/air-quality-api
 */
interface AirQualityApiService {

    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        // "auto" returns times in the location's own time zone
        @Query("timezone") timezone: String = "auto",
        // Two days so a 24 hour outlook still works late in the evening
        @Query("forecast_days") forecastDays: Int = 2
    ): AirQualityResponse

    companion object {
        const val BASE_URL = "https://air-quality-api.open-meteo.com/"
        const val CURRENT_FIELDS = "us_aqi,pm2_5,pm10,uv_index"
        const val HOURLY_FIELDS = "us_aqi,pm2_5,uv_index"
    }
}

/**
 * Retrofit interface for the Open-Meteo Geocoding API, used by the city search in Settings.
 * Docs: https://open-meteo.com/en/docs/geocoding-api
 */
interface GeocodingApiService {

    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 6,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse

    companion object {
        const val BASE_URL = "https://geocoding-api.open-meteo.com/"
    }
}
