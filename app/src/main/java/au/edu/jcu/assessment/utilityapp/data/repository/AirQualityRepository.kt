package au.edu.jcu.assessment.utilityapp.data.repository

import au.edu.jcu.assessment.utilityapp.data.model.AirQualityReport
import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.HourlyReading
import au.edu.jcu.assessment.utilityapp.data.remote.AirQualityApiService
import au.edu.jcu.assessment.utilityapp.data.remote.AirQualityResponse
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * Single source of truth for haze and UV data.
 * The ViewModel only knows about this interface, so the data source can be swapped
 * (for example with a fake in tests) without touching the UI.
 */
interface AirQualityRepository {
    suspend fun getReport(city: City): AirQualityReport
}

/** Fetches live data from the Open-Meteo Air Quality API. */
class NetworkAirQualityRepository(
    private val api: AirQualityApiService
) : AirQualityRepository {

    override suspend fun getReport(city: City): AirQualityReport =
        api.getAirQuality(latitude = city.latitude, longitude = city.longitude).toReport()
}

/** Converts the raw network response into the app's own clean model. */
internal fun AirQualityResponse.toReport(): AirQualityReport {
    val now = current ?: throw IllegalStateException("No current air quality data for this place")

    // Hourly values are parallel lists, so zip them together by index
    val readings = hourly?.let { h ->
        h.time.mapIndexed { index, time ->
            HourlyReading(
                time = LocalDateTime.parse(time),
                usAqi = h.usAqi.getOrNull(index)?.roundToInt(),
                pm25 = h.pm25.getOrNull(index),
                uvIndex = h.uvIndex.getOrNull(index)
            )
        }
    } ?: emptyList()

    return AirQualityReport(
        observedAt = if (now.time.isNotBlank()) LocalDateTime.parse(now.time) else LocalDateTime.now(),
        usAqi = now.usAqi?.roundToInt() ?: readings.firstOrNull { it.usAqi != null }?.usAqi ?: 0,
        pm25 = now.pm25 ?: 0.0,
        pm10 = now.pm10 ?: 0.0,
        uvIndex = now.uvIndex ?: 0.0,
        hourly = readings
    )
}
