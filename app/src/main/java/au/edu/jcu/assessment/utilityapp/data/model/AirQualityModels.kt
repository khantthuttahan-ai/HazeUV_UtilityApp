package au.edu.jcu.assessment.utilityapp.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * A place the user can check haze and UV for.
 *
 * @property region State or province (e.g. "Selangor"), when the API provides one.
 */
data class City(
    val name: String,
    val country: String?,
    val region: String?,
    val latitude: Double,
    val longitude: Double
) {
    /** Short label such as "Seoul, South Korea" (or just "Singapore"). */
    val displayName: String
        get() = listOfNotNull(name, country).distinct().joinToString(", ")

    /** Second line shown under a search result, e.g. "Selangor, Malaysia". */
    val subtitle: String
        get() = listOfNotNull(region, country).distinct().joinToString(", ")

    companion object {
        /** Default location when the app starts. */
        val Singapore = City("Singapore", "Singapore", null, 1.2897, 103.8501)

        /** Quick picks shown in Settings so most users never need to type. */
        val Presets = listOf(
            Singapore,
            City("Seoul", "South Korea", null, 37.5665, 126.9780),
            City("Kuala Lumpur", "Malaysia", null, 3.1390, 101.6869),
            City("Jakarta", "Indonesia", null, -6.2088, 106.8456),
            City("Bangkok", "Thailand", null, 13.7563, 100.5018),
            City("Townsville", "Australia", null, -19.2590, 146.8169)
        )
    }
}

/** One hour of forecast data. The API can leave gaps, so values are nullable. */
data class HourlyReading(
    val time: LocalDateTime,
    val usAqi: Int?,
    val pm25: Double?,
    val uvIndex: Double?
)

/**
 * Clean, UI-ready snapshot of air quality and UV for one location.
 * All times are in the location's own local time.
 */
data class AirQualityReport(
    val observedAt: LocalDateTime,
    val usAqi: Int,
    val pm25: Double,
    val pm10: Double,
    val uvIndex: Double,
    val hourly: List<HourlyReading>
) {
    /** Readings from the current hour onwards, limited to [hours] entries. */
    fun outlook(hours: Int): List<HourlyReading> {
        val startOfHour = observedAt.truncatedTo(ChronoUnit.HOURS)
        return hourly.filter { !it.time.isBefore(startOfHour) }.take(hours)
    }

    /** The hour with the strongest UV on the same day as [observedAt], if known. */
    val peakUvToday: HourlyReading?
        get() {
            val today: LocalDate = observedAt.toLocalDate()
            return hourly
                .filter { it.time.toLocalDate() == today && it.uvIndex != null }
                .maxByOrNull { it.uvIndex ?: 0.0 }
        }
}
