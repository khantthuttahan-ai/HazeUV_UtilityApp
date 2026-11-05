package au.edu.jcu.assessment.utilityapp.domain

import au.edu.jcu.assessment.utilityapp.data.model.AirLevel
import au.edu.jcu.assessment.utilityapp.data.model.Assessment
import au.edu.jcu.assessment.utilityapp.data.model.HourlyReading
import au.edu.jcu.assessment.utilityapp.data.model.Sensitivity
import au.edu.jcu.assessment.utilityapp.data.model.UvLevel
import au.edu.jcu.assessment.utilityapp.data.model.Verdict
import kotlin.math.roundToInt

/**
 * Turns raw numbers (AQI and UV index) into plain advice.
 *
 * This is pure Kotlin with no Android code, which keeps the business rules
 * easy to read and easy to unit test.
 */
object AirAdvisor {

    private const val MAX_TIPS = 4

    /** Hours of the day worth suggesting for going out (7 AM to 9 PM). */
    private val SUGGESTED_HOURS = 7..21

    /** Finds the US AQI band for a value, e.g. 42 -> GOOD, 120 -> UNHEALTHY_FOR_SENSITIVE. */
    fun airLevelFor(usAqi: Int): AirLevel =
        AirLevel.entries.first { usAqi <= it.maxAqi }

    /** UV index is reported as a whole number, so round first (2.6 counts as 3, which is Moderate). */
    fun uvLevelFor(uvIndex: Double): UvLevel {
        val rounded = uvIndex.roundToInt()
        return UvLevel.entries.first { rounded <= it.maxUv }
    }

    /**
     * Combines haze and UV into one verdict. The worse of the two always wins,
     * and the Sensitive setting moves each warning one step earlier.
     */
    fun assess(usAqi: Int, uvIndex: Double, sensitivity: Sensitivity): Assessment {
        val airLevel = airLevelFor(usAqi)
        val uvLevel = uvLevelFor(uvIndex)
        val airVerdict = airVerdict(airLevel, sensitivity)
        val uvVerdict = uvVerdict(uvLevel, sensitivity)

        return Assessment(
            verdict = maxOf(airVerdict, uvVerdict),
            airLevel = airLevel,
            uvLevel = uvLevel,
            summary = "Air quality is ${airLevel.label.lowercase()} and UV is ${uvLevel.label.lowercase()}.",
            tips = (airTips(airLevel, airVerdict) + uvTips(uvLevel, uvVerdict)).take(MAX_TIPS)
        )
    }

    /**
     * Picks the best upcoming daytime hour (7 AM to 9 PM) to be outside.
     * Ranked by verdict first, then cleanest air, then weakest UV.
     * Returns null when none of the hours are daytime hours with data.
     */
    fun bestHourOutside(readings: List<HourlyReading>, sensitivity: Sensitivity): HourlyReading? =
        readings
            .filter { it.time.hour in SUGGESTED_HOURS && it.usAqi != null && it.uvIndex != null }
            .minWithOrNull(
                compareBy<HourlyReading>(
                    { assess(it.usAqi ?: 0, it.uvIndex ?: 0.0, sensitivity).verdict },
                    { it.usAqi },
                    { it.uvIndex }
                )
            )

    private fun airVerdict(level: AirLevel, sensitivity: Sensitivity): Verdict {
        val sensitive = sensitivity == Sensitivity.SENSITIVE
        return when (level) {
            AirLevel.GOOD -> Verdict.GOOD
            AirLevel.MODERATE -> if (sensitive) Verdict.CAUTION else Verdict.GOOD
            AirLevel.UNHEALTHY_FOR_SENSITIVE -> if (sensitive) Verdict.PROTECT else Verdict.CAUTION
            AirLevel.UNHEALTHY -> if (sensitive) Verdict.STAY_IN else Verdict.PROTECT
            AirLevel.VERY_UNHEALTHY, AirLevel.HAZARDOUS -> Verdict.STAY_IN
        }
    }

    private fun uvVerdict(level: UvLevel, sensitivity: Sensitivity): Verdict {
        val sensitive = sensitivity == Sensitivity.SENSITIVE
        return when (level) {
            UvLevel.LOW -> Verdict.GOOD
            UvLevel.MODERATE -> if (sensitive) Verdict.CAUTION else Verdict.GOOD
            UvLevel.HIGH -> if (sensitive) Verdict.PROTECT else Verdict.CAUTION
            UvLevel.VERY_HIGH, UvLevel.EXTREME -> Verdict.PROTECT
        }
    }

    private fun airTips(level: AirLevel, verdict: Verdict): List<String> = when (verdict) {
        Verdict.GOOD ->
            if (level == AirLevel.GOOD) listOf("Air is clean, a good time to open the windows")
            else listOf("Air is fine for most people")
        Verdict.CAUTION -> listOf("Cut back on long or intense outdoor exercise")
        Verdict.PROTECT -> listOf("Wear an N95 mask if you head outside", "Keep outdoor time short")
        Verdict.STAY_IN -> listOf("Stay indoors and keep windows closed", "Run an air purifier if you have one")
    }

    private fun uvTips(level: UvLevel, verdict: Verdict): List<String> = when (verdict) {
        Verdict.GOOD ->
            if (level == UvLevel.LOW) listOf("No sun protection needed right now")
            else listOf("Sunglasses help on bright days")
        Verdict.CAUTION -> listOf("Put on SPF 30+ sunscreen")
        Verdict.PROTECT, Verdict.STAY_IN ->
            listOf("Put on SPF 30+ sunscreen", "Seek shade between 11am and 3pm")
    }
}
