package au.edu.jcu.assessment.utilityapp.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.assessment.utilityapp.data.model.AppSettings
import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.Sensitivity
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import au.edu.jcu.assessment.utilityapp.ui.viewmodel.HazeViewModel
import au.edu.jcu.assessment.utilityapp.ui.viewmodel.SearchUiState

/**
 * Settings screen. Every option here changes what the Today screen shows.
 * Settings live in the shared ViewModel, so they are not saved after the app closes.
 */
@Composable
fun SettingsScreen(viewModel: HazeViewModel, modifier: Modifier = Modifier) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()

    SettingsContent(
        settings = settings,
        search = search,
        onQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::clearSearch,
        onCitySelected = viewModel::selectCity,
        onSensitivityChange = viewModel::setSensitivity,
        onShowHazeChange = viewModel::setShowHaze,
        onShowUvChange = viewModel::setShowUv,
        onShowOutlookChange = viewModel::setShowOutlook,
        onOutlookHoursChange = viewModel::setOutlookHours,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    settings: AppSettings,
    search: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCitySelected: (City) -> Unit,
    onSensitivityChange: (Sensitivity) -> Unit,
    onShowHazeChange: (Boolean) -> Unit,
    onShowUvChange: (Boolean) -> Unit,
    onShowOutlookChange: (Boolean) -> Unit,
    onOutlookHoursChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Changes show on the Today screen straight away. Settings reset when the app is closed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ---------- Location ----------
        SettingsSection(title = "Location", icon = Icons.Rounded.LocationOn) {
            Text(
                text = "Showing ${settings.city.displayName}",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = search.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search any city") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (search.query.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            if (search.isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            search.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            search.results.forEach { city ->
                ListItem(
                    headlineContent = { Text(city.name) },
                    supportingContent = { Text(city.subtitle) },
                    leadingContent = { Icon(Icons.Rounded.Place, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            focusManager.clearFocus()
                            onCitySelected(city)
                        }
                )
            }

            Text(
                text = "Quick picks",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(City.Presets, key = { it.name }) { city ->
                    val selected = city == settings.city
                    FilterChip(
                        selected = selected,
                        onClick = { onCitySelected(city) },
                        label = { Text(city.name) },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }

        // ---------- Sensitivity ----------
        SettingsSection(title = "Sensitivity", icon = Icons.Rounded.Favorite) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Sensitivity.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = settings.sensitivity == option,
                        onClick = { onSensitivityChange(option) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = Sensitivity.entries.size
                        )
                    ) {
                        Text(option.label)
                    }
                }
            }
            Text(
                text = settings.sensitivity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- What the Today screen shows ----------
        SettingsSection(title = "Today screen", icon = Icons.Rounded.Dashboard) {
            SwitchRow(
                title = "Haze card",
                subtitle = "US AQI, PM2.5 and PM10",
                checked = settings.showHaze,
                onCheckedChange = onShowHazeChange
            )
            SwitchRow(
                title = "UV card",
                subtitle = "Current UV and today's peak",
                checked = settings.showUv,
                onCheckedChange = onShowUvChange
            )
            SwitchRow(
                title = "Hourly outlook",
                subtitle = "Coming hours plus the best time to head out",
                checked = settings.showOutlook,
                onCheckedChange = onShowOutlookChange
            )

            Text(
                text = "Outlook length",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AppSettings.OutlookOptions.forEachIndexed { index, hours ->
                    SegmentedButton(
                        selected = settings.outlookHours == hours,
                        onClick = { onOutlookHoursChange(hours) },
                        enabled = settings.showOutlook,
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = AppSettings.OutlookOptions.size
                        )
                    ) {
                        Text("$hours hrs")
                    }
                }
            }
        }

        // ---------- About ----------
        SettingsSection(title = "About", icon = Icons.Rounded.Info) {
            Text(
                text = "Haze & UV Check uses the free Open-Meteo Air Quality and Geocoding APIs. " +
                        "Haze follows the US AQI scale and UV follows the WHO UV index.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Rounded card with an icon and title, used to group related settings. */
@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

/** A whole row that toggles the switch, giving a bigger and easier touch target. */
@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

// ---------- Preview (Android Studio design view only) ----------

@Preview(name = "Light", showBackground = true, heightDp = 1300)
@Preview(name = "Dark", showBackground = true, heightDp = 1300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsContentPreview() {
    MobileTech_UtilityAppTheme {
        Surface {
            SettingsContent(
                settings = AppSettings(),
                search = SearchUiState(
                    query = "Seo",
                    results = listOf(City("Seoul", "South Korea", "Seoul", 37.5665, 126.9780))
                ),
                onQueryChange = {},
                onClearSearch = {},
                onCitySelected = {},
                onSensitivityChange = {},
                onShowHazeChange = {},
                onShowUvChange = {},
                onShowOutlookChange = {},
                onOutlookHoursChange = {}
            )
        }
    }
}
