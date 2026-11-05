package au.edu.jcu.assessment.utilityapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import au.edu.jcu.assessment.utilityapp.UtilityApplication
import au.edu.jcu.assessment.utilityapp.data.model.AirQualityReport
import au.edu.jcu.assessment.utilityapp.data.model.AppSettings
import au.edu.jcu.assessment.utilityapp.data.model.Assessment
import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.HourlyReading
import au.edu.jcu.assessment.utilityapp.data.model.Sensitivity
import au.edu.jcu.assessment.utilityapp.data.repository.AirQualityRepository
import au.edu.jcu.assessment.utilityapp.data.repository.LocationRepository
import au.edu.jcu.assessment.utilityapp.domain.AirAdvisor
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** What the main (Today) screen should show. */
sealed interface AirUiState {
    data object Loading : AirUiState

    /**
     * @property outlook the coming hours, already trimmed to the length chosen in Settings
     * @property bestHour the best daytime hour in [outlook] to go outside, if there is one
     */
    data class Success(
        val report: AirQualityReport,
        val assessment: Assessment,
        val outlook: List<HourlyReading>,
        val bestHour: HourlyReading?
    ) : AirUiState

    data class Error(val message: String) : AirUiState
}

/** State of the city search box on the Settings screen. */
data class SearchUiState(
    val query: String = "",
    val results: List<City> = emptyList(),
    val isSearching: Boolean = false,
    val message: String? = null
)

/** Raw result of the last network request, before settings are applied. */
private sealed interface ReportResult {
    data object Loading : ReportResult
    data class Loaded(val report: AirQualityReport) : ReportResult
    data class Failed(val message: String) : ReportResult
}

/**
 * Shared by both tabs, so a change made in Settings shows up on the Today screen straight away.
 * Holds all UI state in StateFlows, which survive configuration changes such as rotation.
 */
class HazeViewModel(
    private val airQualityRepository: AirQualityRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** Message shown when a refresh fails but older data is still on screen. */
    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()

    private val reportResult = MutableStateFlow<ReportResult>(ReportResult.Loading)

    /**
     * The network result combined with the current settings.
     * Changing sensitivity re-runs the advice instantly without another network call.
     */
    val uiState: StateFlow<AirUiState> = combine(reportResult, _settings) { result, settings ->
        when (result) {
            ReportResult.Loading -> AirUiState.Loading
            is ReportResult.Failed -> AirUiState.Error(result.message)
            is ReportResult.Loaded -> {
                val outlook = result.report.outlook(settings.outlookHours)
                AirUiState.Success(
                    report = result.report,
                    assessment = AirAdvisor.assess(
                        usAqi = result.report.usAqi,
                        uvIndex = result.report.uvIndex,
                        sensitivity = settings.sensitivity
                    ),
                    outlook = outlook,
                    bestHour = AirAdvisor.bestHourOutside(outlook, settings.sensitivity)
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AirUiState.Loading
    )

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    /** When the last successful download finished, used to decide if the data is stale. */
    private var lastUpdatedMillis = 0L

    init {
        loadReport(keepCurrentData = false)
    }

    /** Called by the refresh button. Keeps the old data on screen while loading. */
    fun refresh() {
        loadReport(keepCurrentData = true)
    }

    /**
     * Called whenever the Today screen is resumed (see UtilityScreen).
     * Reloads only if the data is older than [STALE_AFTER_MS], or retries after an error,
     * so reopening the app later always shows fresh numbers without wasting requests.
     */
    fun refreshIfStale() {
        when (reportResult.value) {
            is ReportResult.Loaded -> {
                if (System.currentTimeMillis() - lastUpdatedMillis > STALE_AFTER_MS) refresh()
            }
            is ReportResult.Failed -> loadReport(keepCurrentData = false)
            ReportResult.Loading -> Unit // a request is already running
        }
    }

    private fun loadReport(keepCurrentData: Boolean) {
        loadJob?.cancel() // only the newest request matters
        val city = _settings.value.city
        val previous = reportResult.value

        loadJob = viewModelScope.launch {
            val hasData = keepCurrentData && previous is ReportResult.Loaded
            if (hasData) {
                _isRefreshing.value = true
            } else {
                _refreshError.value = null
                reportResult.value = ReportResult.Loading
            }

            val result = try {
                ReportResult.Loaded(airQualityRepository.getReport(city))
            } catch (e: CancellationException) {
                throw e // never swallow coroutine cancellation
            } catch (e: IOException) {
                ReportResult.Failed("No internet connection. Check your network and try again.")
            } catch (e: HttpException) {
                ReportResult.Failed("The air quality service is busy (error ${e.code()}). Please try again.")
            } catch (e: Exception) {
                ReportResult.Failed("Something went wrong while reading the air quality data.")
            }

            when {
                result is ReportResult.Loaded -> {
                    lastUpdatedMillis = System.currentTimeMillis()
                    _refreshError.value = null
                    reportResult.value = result
                }
                // A failed refresh keeps the last reading on screen and shows a small note
                hasData && result is ReportResult.Failed -> _refreshError.value = result.message
                // Nothing useful on screen yet, so show the full error card
                else -> reportResult.value = result
            }
            _isRefreshing.value = false
        }
    }

    // ---------- Settings actions ----------

    fun selectCity(city: City) {
        clearSearch()
        if (city == _settings.value.city) return
        _settings.update { it.copy(city = city) }
        loadReport(keepCurrentData = false) // new place, so don't show the old city's numbers
    }

    fun setSensitivity(sensitivity: Sensitivity) {
        _settings.update { it.copy(sensitivity = sensitivity) }
    }

    fun setShowHaze(show: Boolean) {
        _settings.update { it.copy(showHaze = show) }
    }

    fun setShowUv(show: Boolean) {
        _settings.update { it.copy(showUv = show) }
    }

    fun setShowOutlook(show: Boolean) {
        _settings.update { it.copy(showOutlook = show) }
    }

    fun setOutlookHours(hours: Int) {
        _settings.update { it.copy(outlookHours = hours) }
    }

    // ---------- City search ----------

    /** Searches as the user types, waiting briefly so we don't call the API on every key press. */
    fun onSearchQueryChange(query: String) {
        _search.update { it.copy(query = query, message = null) }
        searchJob?.cancel()

        if (query.trim().length < MIN_QUERY_LENGTH) {
            _search.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _search.update { it.copy(isSearching = true) }
            try {
                val cities = locationRepository.searchCities(query)
                _search.update {
                    it.copy(
                        results = cities,
                        isSearching = false,
                        message = if (cities.isEmpty()) "No places found for \"${query.trim()}\"" else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _search.update {
                    it.copy(
                        results = emptyList(),
                        isSearching = false,
                        message = "Couldn't search right now. Check your connection."
                    )
                }
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _search.value = SearchUiState()
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 2
        private const val SEARCH_DEBOUNCE_MS = 400L
        private const val STALE_AFTER_MS = 15 * 60 * 1000L // 15 minutes

        /** Builds the ViewModel with repositories from the app's DI container. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as UtilityApplication
                HazeViewModel(
                    airQualityRepository = app.container.airQualityRepository,
                    locationRepository = app.container.locationRepository
                )
            }
        }
    }
}
