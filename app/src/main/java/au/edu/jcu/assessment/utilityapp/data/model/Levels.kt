package au.edu.jcu.assessment.utilityapp.data.model

/**
 * US EPA Air Quality Index bands.
 * @property maxAqi Highest AQI value that still belongs to this band.
 */
enum class AirLevel(val label: String, val maxAqi: Int) {
    GOOD("Good", 50),
    MODERATE("Moderate", 100),
    UNHEALTHY_FOR_SENSITIVE("Unhealthy for sensitive groups", 150),
    UNHEALTHY("Unhealthy", 200),
    VERY_UNHEALTHY("Very unhealthy", 300),
    HAZARDOUS("Hazardous", Int.MAX_VALUE)
}

/**
 * WHO UV index bands.
 * @property maxUv Highest (rounded) UV index that still belongs to this band.
 */
enum class UvLevel(val label: String, val maxUv: Int) {
    LOW("Low", 2),
    MODERATE("Moderate", 5),
    HIGH("High", 7),
    VERY_HIGH("Very high", 10),
    EXTREME("Extreme", Int.MAX_VALUE)
}

/** How cautious the advice should be. Chosen by the user in Settings. */
enum class Sensitivity(val label: String, val description: String) {
    STANDARD("Standard", "Advice for healthy adults."),
    SENSITIVE(
        "Sensitive",
        "Earlier warnings for asthma, young children, older adults or fair skin."
    )
}

/** The one-line answer shown at the top of the main screen. Ordered from best to worst. */
enum class Verdict(val headline: String) {
    GOOD("Good to go"),
    CAUTION("Go out with care"),
    PROTECT("Protect yourself"),
    STAY_IN("Best to stay indoors")
}

/** Result of combining haze, UV and the user's sensitivity into simple advice. */
data class Assessment(
    val verdict: Verdict,
    val airLevel: AirLevel,
    val uvLevel: UvLevel,
    val summary: String,
    val tips: List<String>
)

/**
 * Everything the user can change on the Settings screen.
 * Kept in memory only, so it resets when the app is closed (allowed by the brief).
 */
data class AppSettings(
    val city: City = City.Singapore,
    val sensitivity: Sensitivity = Sensitivity.STANDARD,
    val showHaze: Boolean = true,
    val showUv: Boolean = true,
    val showOutlook: Boolean = true,
    val outlookHours: Int = 12
) {
    companion object {
        /** Choices for the hourly outlook length, in hours. */
        val OutlookOptions = listOf(6, 12, 24)
    }
}
