package au.edu.jcu.assessment.utilityapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Data transfer objects that mirror the JSON returned by the Open-Meteo APIs.
 * Every field has a default so a missing key never crashes the app.
 */

/** Response from https://air-quality-api.open-meteo.com/v1/air-quality */
@Serializable
data class AirQualityResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String = "",
    val current: CurrentAirQuality? = null,
    val hourly: HourlyAirQuality? = null
)

@Serializable
data class CurrentAirQuality(
    val time: String = "",
    @SerialName("us_aqi") val usAqi: Double? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    val pm10: Double? = null,
    @SerialName("uv_index") val uvIndex: Double? = null
)

/** Hourly values come back as parallel lists that share the same index as [time]. */
@Serializable
data class HourlyAirQuality(
    val time: List<String> = emptyList(),
    @SerialName("us_aqi") val usAqi: List<Double?> = emptyList(),
    @SerialName("pm2_5") val pm25: List<Double?> = emptyList(),
    @SerialName("uv_index") val uvIndex: List<Double?> = emptyList()
)

/** Response from https://geocoding-api.open-meteo.com/v1/search */
@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult> = emptyList()
)

@Serializable
data class GeocodingResult(
    val id: Long = 0,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String? = null,
    @SerialName("admin1") val region: String? = null
)
