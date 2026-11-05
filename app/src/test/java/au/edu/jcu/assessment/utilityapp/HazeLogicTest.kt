package au.edu.jcu.assessment.utilityapp

import au.edu.jcu.assessment.utilityapp.data.model.AirLevel
import au.edu.jcu.assessment.utilityapp.data.model.HourlyReading
import au.edu.jcu.assessment.utilityapp.data.model.Sensitivity
import au.edu.jcu.assessment.utilityapp.data.model.UvLevel
import au.edu.jcu.assessment.utilityapp.data.model.Verdict
import au.edu.jcu.assessment.utilityapp.data.remote.AirQualityResponse
import au.edu.jcu.assessment.utilityapp.data.remote.CurrentAirQuality
import au.edu.jcu.assessment.utilityapp.data.remote.HourlyAirQuality
import au.edu.jcu.assessment.utilityapp.data.repository.toReport
import au.edu.jcu.assessment.utilityapp.domain.AirAdvisor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/** Unit tests for the advice rules and the network-to-model mapping. No device needed. */
class HazeLogicTest {

    @Test
    fun aqiBoundariesMapToCorrectLevels() {
        assertEquals(AirLevel.GOOD, AirAdvisor.airLevelFor(50))
        assertEquals(AirLevel.MODERATE, AirAdvisor.airLevelFor(51))
        assertEquals(AirLevel.UNHEALTHY_FOR_SENSITIVE, AirAdvisor.airLevelFor(101))
        assertEquals(AirLevel.UNHEALTHY, AirAdvisor.airLevelFor(151))
        assertEquals(AirLevel.VERY_UNHEALTHY, AirAdvisor.airLevelFor(201))
        assertEquals(AirLevel.HAZARDOUS, AirAdvisor.airLevelFor(301))
    }

    @Test
    fun uvIndexIsRoundedBeforeClassifying() {
        assertEquals(UvLevel.LOW, AirAdvisor.uvLevelFor(2.4))
        assertEquals(UvLevel.MODERATE, AirAdvisor.uvLevelFor(2.6))
        assertEquals(UvLevel.HIGH, AirAdvisor.uvLevelFor(6.0))
        assertEquals(UvLevel.VERY_HIGH, AirAdvisor.uvLevelFor(8.0))
        assertEquals(UvLevel.EXTREME, AirAdvisor.uvLevelFor(11.2))
    }

    @Test
    fun cleanAirAndLowUvIsGoodToGo() {
        val result = AirAdvisor.assess(usAqi = 30, uvIndex = 1.0, sensitivity = Sensitivity.STANDARD)
        assertEquals(Verdict.GOOD, result.verdict)
        assertTrue(result.tips.isNotEmpty())
    }

    @Test
    fun sensitiveUsersGetEarlierWarnings() {
        assertEquals(Verdict.GOOD, AirAdvisor.assess(80, 0.0, Sensitivity.STANDARD).verdict)
        assertEquals(Verdict.CAUTION, AirAdvisor.assess(80, 0.0, Sensitivity.SENSITIVE).verdict)
    }

    @Test
    fun worseOfHazeAndUvDecidesTheVerdict() {
        assertEquals(Verdict.PROTECT, AirAdvisor.assess(30, 9.0, Sensitivity.STANDARD).verdict)
        assertEquals(Verdict.STAY_IN, AirAdvisor.assess(250, 1.0, Sensitivity.STANDARD).verdict)
    }

    @Test
    fun networkResponseIsMappedToReport() {
        val response = AirQualityResponse(
            current = CurrentAirQuality(
                time = "2026-11-05T14:00",
                usAqi = 68.0,
                pm25 = 20.4,
                pm10 = 31.0,
                uvIndex = 7.6
            ),
            hourly = HourlyAirQuality(
                time = listOf("2026-11-05T13:00", "2026-11-05T14:00", "2026-11-05T15:00"),
                usAqi = listOf(60.0, 68.0, null),
                pm25 = listOf(18.0, 20.4, null),
                uvIndex = listOf(8.0, 7.6, 9.1)
            )
        )

        val report = response.toReport()

        assertEquals(68, report.usAqi)
        assertEquals(2, report.outlook(12).size) // 13:00 is in the past, so it is skipped
        assertEquals(9.1, report.peakUvToday?.uvIndex ?: 0.0, 0.001)
        assertNull(report.hourly[2].usAqi)
    }

    @Test
    fun bestHourSkipsNightAndPicksCleanestDaytimeHour() {
        val day = LocalDateTime.of(2026, 11, 5, 0, 0)
        val readings = listOf(
            HourlyReading(day.withHour(3), usAqi = 20, pm25 = 5.0, uvIndex = 0.0),   // night, ignored
            HourlyReading(day.withHour(9), usAqi = 70, pm25 = 20.0, uvIndex = 3.0),
            HourlyReading(day.withHour(13), usAqi = 40, pm25 = 10.0, uvIndex = 10.0), // clean air but very high UV
            HourlyReading(day.withHour(18), usAqi = 45, pm25 = 12.0, uvIndex = 1.0)   // best
        )

        val best = AirAdvisor.bestHourOutside(readings, Sensitivity.STANDARD)

        assertEquals(18, best?.time?.hour)
        assertNull(AirAdvisor.bestHourOutside(readings.take(1), Sensitivity.STANDARD))
    }
}
