package au.edu.jcu.assessment.utilityapp.ui.theme

import androidx.compose.ui.graphics.Color
import au.edu.jcu.assessment.utilityapp.data.model.AirLevel
import au.edu.jcu.assessment.utilityapp.data.model.UvLevel
import au.edu.jcu.assessment.utilityapp.data.model.Verdict

// ---------- Air quality band colours (based on the official US AQI colour scale) ----------
val AqiGood = Color(0xFF2E9E5B)
val AqiModerate = Color(0xFFF2C230)
val AqiSensitive = Color(0xFFF28C28)
val AqiUnhealthy = Color(0xFFE0453A)
val AqiVeryUnhealthy = Color(0xFF8E44AD)
val AqiHazardous = Color(0xFF7E1F3A)

// ---------- UV band colours (based on the WHO UV index colour scale) ----------
val UvLow = Color(0xFF43A047)
val UvModerate = Color(0xFFF9C80E)
val UvHigh = Color(0xFFF57C00)
val UvVeryHigh = Color(0xFFE53935)
val UvExtreme = Color(0xFF8E24AA)

/** Colour for a single AQI band, used for pills, dots and icons. */
fun AirLevel.color(): Color = when (this) {
    AirLevel.GOOD -> AqiGood
    AirLevel.MODERATE -> AqiModerate
    AirLevel.UNHEALTHY_FOR_SENSITIVE -> AqiSensitive
    AirLevel.UNHEALTHY -> AqiUnhealthy
    AirLevel.VERY_UNHEALTHY -> AqiVeryUnhealthy
    AirLevel.HAZARDOUS -> AqiHazardous
}

/** Colour for a single UV band. */
fun UvLevel.color(): Color = when (this) {
    UvLevel.LOW -> UvLow
    UvLevel.MODERATE -> UvModerate
    UvLevel.HIGH -> UvHigh
    UvLevel.VERY_HIGH -> UvVeryHigh
    UvLevel.EXTREME -> UvExtreme
}

/** Background gradient for the big verdict card. White text stays readable on all of them. */
fun Verdict.gradient(): List<Color> = when (this) {
    Verdict.GOOD -> listOf(Color(0xFF0B7A75), Color(0xFF2A9D8F))
    Verdict.CAUTION -> listOf(Color(0xFF9A5800), Color(0xFFCF850E))
    Verdict.PROTECT -> listOf(Color(0xFFB23A12), Color(0xFFE0573A))
    Verdict.STAY_IN -> listOf(Color(0xFF5B1E7A), Color(0xFF8E2D5E))
}

/** Full colour scales used by the gauges, from best to worst. */
val AirScale: List<Color> = AirLevel.entries.map { it.color() }
val UvScale: List<Color> = UvLevel.entries.map { it.color() }
