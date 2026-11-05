package au.edu.jcu.assessment.utilityapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.data.model.Assessment
import au.edu.jcu.assessment.utilityapp.data.model.HourlyReading
import au.edu.jcu.assessment.utilityapp.data.model.Sensitivity
import au.edu.jcu.assessment.utilityapp.data.model.Verdict
import au.edu.jcu.assessment.utilityapp.domain.AirAdvisor
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import au.edu.jcu.assessment.utilityapp.ui.theme.color
import au.edu.jcu.assessment.utilityapp.ui.theme.gradient
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Shared time formats, e.g. "2 PM" and "2:00 PM". */
object TimeFormats {
    val hour: DateTimeFormatter = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH)
    val clock: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
}

/**
 * Hero card that answers "Is it OK to go outside?" in one glance.
 * The whole card is read as one item by TalkBack.
 */
@Composable
fun VerdictCard(assessment: Assessment, modifier: Modifier = Modifier) {
    val verdict = assessment.verdict
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(verdict.gradient()))
            .semantics(mergeDescendants = true) {}
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(verdict.icon(), contentDescription = null, tint = Color.White)
                }
                Text(
                    text = "RIGHT NOW",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Text(
                text = verdict.headline,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Text(
                text = assessment.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.92f)
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
            assessment.tips.forEach { tip ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun Verdict.icon(): ImageVector = when (this) {
    Verdict.GOOD -> Icons.Rounded.SentimentSatisfied
    Verdict.CAUTION -> Icons.Rounded.Info
    Verdict.PROTECT -> Icons.Rounded.Warning
    Verdict.STAY_IN -> Icons.Rounded.Home
}

/**
 * Reusable card for one measurement (used for both Haze and UV).
 *
 * @param scale colours of the gauge from best to worst
 * @param position where the marker sits on the gauge, from 0 to 1
 * @param details extra label/value rows shown under the gauge
 */
@Composable
fun MetricCard(
    title: String,
    icon: ImageVector,
    value: String,
    unit: String,
    levelLabel: String,
    levelColor: Color,
    scale: List<Color>,
    position: Float,
    details: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconBadge(icon = icon, color = levelColor)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            LevelPill(text = levelLabel, color = levelColor)
            LevelGauge(colors = scale, position = position)

            details.forEach { (label, detailValue) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = detailValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/** Round icon with a soft tinted background. */
@Composable
fun IconBadge(icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color)
    }
}

/** Solid coloured label. Text switches between black and white so it is always readable. */
@Composable
fun LevelPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = color, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (color.luminance() > 0.5f) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/** Colour bar with an animated marker showing where the current value sits on the scale. */
@Composable
fun LevelGauge(colors: List<Color>, position: Float, modifier: Modifier = Modifier) {
    val animatedPosition by animateFloatAsState(
        targetValue = position.coerceIn(0f, 1f),
        label = "gaugePosition"
    )
    val markerSize = 18.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(markerSize)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(colors))
        )
        Box(
            modifier = Modifier
                .offset(x = (maxWidth - markerSize) * animatedPosition)
                .size(markerSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(4.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
        )
    }
}

/**
 * Horizontally scrolling list of the coming hours.
 * The best hour to go outside is called out above the list and outlined in the list.
 */
@Composable
fun HourlyOutlookCard(
    readings: List<HourlyReading>,
    bestHour: HourlyReading?,
    showHaze: Boolean,
    showUv: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconBadge(icon = Icons.Rounded.Schedule, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Next ${readings.size} hours",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (bestHour != null) {
                BestHourBanner(
                    label = if (bestHour == readings.firstOrNull()) "Right now"
                    else bestHour.time.format(TimeFormats.hour),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(readings, key = { it.time.toString() }) { reading ->
                    HourChip(
                        reading = reading,
                        isNow = reading == readings.first(),
                        isBest = reading == bestHour,
                        showHaze = showHaze,
                        showUv = showUv
                    )
                }
            }
        }
    }
}

/** "Best time to head out" call-out shown above the hourly list. */
@Composable
private fun BestHourBanner(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = "Best time to head out",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun HourChip(
    reading: HourlyReading,
    isNow: Boolean,
    isBest: Boolean,
    showHaze: Boolean,
    showUv: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isNow) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        border = if (isBest) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .width(76.dp)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isNow) "Now" else reading.time.format(TimeFormats.hour),
                style = MaterialTheme.typography.labelLarge
            )
            if (showHaze) {
                val aqi = reading.usAqi
                LevelPill(
                    text = aqi?.toString() ?: "-",
                    color = if (aqi != null) AirAdvisor.airLevelFor(aqi).color()
                    else MaterialTheme.colorScheme.outline
                )
            }
            if (showUv) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = "UV",
                        tint = reading.uvIndex?.let { AirAdvisor.uvLevelFor(it).color() }
                            ?: MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = reading.uvIndex?.roundToInt()?.toString() ?: "-",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/** Shown while the first request is in flight. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Checking the air...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Friendly error with a retry button, so a bad connection never leaves a blank screen. */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Text(text = "Couldn't load air quality", style = MaterialTheme.typography.titleMedium)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try again")
            }
        }
    }
}

// ---------- Preview (Android Studio design view only) ----------

/** Shows all four verdict styles at once, handy for checking colours and text. */
@Preview(name = "Verdict cards", showBackground = true, widthDp = 380, heightDp = 1500)
@Composable
private fun VerdictCardPreview() {
    MobileTech_UtilityAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // (US AQI, UV index) pairs that produce each verdict
            listOf(30 to 1.0, 120 to 4.0, 170 to 9.0, 260 to 2.0).forEach { (aqi, uv) ->
                VerdictCard(assessment = AirAdvisor.assess(aqi, uv, Sensitivity.STANDARD))
            }
        }
    }
}
