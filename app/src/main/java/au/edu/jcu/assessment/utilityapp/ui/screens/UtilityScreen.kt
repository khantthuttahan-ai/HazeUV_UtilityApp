package au.edu.jcu.assessment.utilityapp.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.assessment.utilityapp.data.model.AirQualityReport
import au.edu.jcu.assessment.utilityapp.data.model.AppSettings
import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.HourlyReading
import au.edu.jcu.assessment.utilityapp.data.model.Sensitivity
import au.edu.jcu.assessment.utilityapp.data.model.UvLevel
import au.edu.jcu.assessment.utilityapp.domain.AirAdvisor
import au.edu.jcu.assessment.utilityapp.ui.components.ErrorState
import au.edu.jcu.assessment.utilityapp.ui.components.HourlyOutlookCard
import au.edu.jcu.assessment.utilityapp.ui.components.LoadingState
import au.edu.jcu.assessment.utilityapp.ui.components.MetricCard
import au.edu.jcu.assessment.utilityapp.ui.components.TimeFormats
import au.edu.jcu.assessment.utilityapp.ui.components.VerdictCard
import au.edu.jcu.assessment.utilityapp.ui.theme.AirScale
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import au.edu.jcu.assessment.utilityapp.ui.theme.UvScale
import au.edu.jcu.assessment.utilityapp.ui.theme.color
import au.edu.jcu.assessment.utilityapp.ui.viewmodel.AirUiState
import au.edu.jcu.assessment.utilityapp.ui.viewmodel.HazeViewModel
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** Top of the US AQI gauge. Values above this sit at the far right. */
private const val AQI_GAUGE_MAX = 300f

/** Top of the UV gauge (11+ is Extreme). */
private const val UV_GAUGE_MAX = 12.0

/**
 * Main "Today" screen. Reads state from the ViewModel and passes it down
 * to the stateless [UtilityContent], which is easy to preview and reuse.
 */
@Composable
fun UtilityScreen(viewModel: HazeViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val refreshError by viewModel.refreshError.collectAsStateWithLifecycle()

    // Lifecycle-aware refresh: each time this screen is resumed (app reopened,
    // or user switches back to this tab) reload only if the data is old or failed.
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshIfStale()
        onPauseOrDispose { }
    }

    UtilityContent(
        uiState = uiState,
        settings = settings,
        isRefreshing = isRefreshing,
        refreshError = refreshError,
        onRefresh = viewModel::refresh,
        modifier = modifier
    )
}

@Composable
fun UtilityContent(
    uiState: AirUiState,
    settings: AppSettings,
    isRefreshing: Boolean,
    refreshError: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(
            city = settings.city,
            observedAt = (uiState as? AirUiState.Success)?.report?.observedAt,
            isRefreshing = isRefreshing,
            refreshError = refreshError,
            onRefresh = onRefresh
        )

        when (uiState) {
            AirUiState.Loading -> LoadingState()
            is AirUiState.Error -> ErrorState(message = uiState.message, onRetry = onRefresh)
            is AirUiState.Success -> AirQualityDetails(state = uiState, settings = settings)
        }

        // Open-Meteo data is CC BY 4.0, so we credit the source
        Text(
            text = "Air quality and UV data by Open-Meteo.com",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/** City name, last update time and a refresh button. */
@Composable
private fun ScreenHeader(
    city: City,
    observedAt: LocalDateTime?,
    isRefreshing: Boolean,
    refreshError: String?,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Haze & UV",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = observedAt?.let { "Latest reading at ${it.format(TimeFormats.clock)} local time" }
                    ?: city.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (refreshError != null) {
                Text(
                    text = "Couldn't refresh, showing the last reading",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        FilledTonalIconButton(onClick = onRefresh, enabled = !isRefreshing) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
            }
        }
    }
}

/** Verdict, then the cards the user has switched on in Settings. */
@Composable
private fun AirQualityDetails(state: AirUiState.Success, settings: AppSettings) {
    val report = state.report
    val assessment = state.assessment

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        VerdictCard(assessment = assessment)

        if (settings.showHaze) {
            MetricCard(
                title = "Haze",
                icon = Icons.Rounded.Air,
                value = report.usAqi.toString(),
                unit = "US AQI",
                levelLabel = assessment.airLevel.label,
                levelColor = assessment.airLevel.color(),
                scale = AirScale,
                position = report.usAqi / AQI_GAUGE_MAX,
                details = listOf(
                    "Fine particles (PM2.5)" to formatMicrograms(report.pm25),
                    "Coarse particles (PM10)" to formatMicrograms(report.pm10)
                )
            )
        }

        if (settings.showUv) {
            val peak = report.peakUvToday
            MetricCard(
                title = "UV index",
                icon = Icons.Rounded.WbSunny,
                value = report.uvIndex.roundToInt().toString(),
                unit = "UV",
                levelLabel = assessment.uvLevel.label,
                levelColor = assessment.uvLevel.color(),
                scale = UvScale,
                position = (report.uvIndex / UV_GAUGE_MAX).toFloat(),
                details = listOfNotNull(
                    peak?.let {
                        "Peak today" to "UV ${it.uvIndex?.roundToInt()} at ${it.time.format(TimeFormats.hour)}"
                    },
                    "Sun protection" to sunProtectionText(assessment.uvLevel)
                )
            )
        }

        // The outlook only makes sense if at least one measurement is visible
        if (settings.showOutlook && (settings.showHaze || settings.showUv) && state.outlook.isNotEmpty()) {
            HourlyOutlookCard(
                readings = state.outlook,
                bestHour = state.bestHour,
                showHaze = settings.showHaze,
                showUv = settings.showUv
            )
        }
    }
}

private fun formatMicrograms(value: Double): String =
    String.format(Locale.ENGLISH, "%.1f µg/m³", value)

/** WHO advises sun protection from UV 3 (Moderate) upwards. */
private fun sunProtectionText(level: UvLevel): String =
    if (level >= UvLevel.MODERATE) "Recommended" else "Not needed"

// ---------- Previews (Android Studio design view only) ----------

private val previewReport = AirQualityReport(
    observedAt = LocalDateTime.of(2026, 11, 5, 14, 0),
    usAqi = 68,
    pm25 = 20.4,
    pm10 = 31.2,
    uvIndex = 7.6,
    hourly = (0 until 36).map { hour ->
        val time = LocalDateTime.of(2026, 11, 5, 0, 0).plusHours(hour.toLong())
        val hourOfDay = time.hour
        HourlyReading(
            time = time,
            usAqi = 45 + (hour * 7) % 60,
            pm25 = 10.0 + hour % 12,
            uvIndex = if (hourOfDay in 7..18) (10.0 - abs(13 - hourOfDay) * 1.6).coerceAtLeast(0.0) else 0.0
        )
    }
)

private fun previewState(): AirUiState.Success {
    val outlook = previewReport.outlook(12)
    return AirUiState.Success(
        report = previewReport,
        assessment = AirAdvisor.assess(68, 7.6, Sensitivity.STANDARD),
        outlook = outlook,
        bestHour = AirAdvisor.bestHourOutside(outlook, Sensitivity.STANDARD)
    )
}

@Preview(name = "Light", showBackground = true, heightDp = 1500)
@Preview(name = "Dark", showBackground = true, heightDp = 1500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UtilityContentPreview() {
    MobileTech_UtilityAppTheme {
        Surface {
            UtilityContent(
                uiState = previewState(),
                settings = AppSettings(),
                isRefreshing = false,
                refreshError = null,
                onRefresh = {}
            )
        }
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun UtilityErrorPreview() {
    MobileTech_UtilityAppTheme {
        Surface {
            UtilityContent(
                uiState = AirUiState.Error("No internet connection. Check your network and try again."),
                settings = AppSettings(),
                isRefreshing = false,
                refreshError = null,
                onRefresh = {}
            )
        }
    }
}
