# Haze & UV Check

A utility app that answers one everyday question in a single glance:
**"Is it OK to go outside right now?"**

In Singapore and across Southeast Asia, seasonal haze and very strong UV are part of daily life.
Haze & UV Check combines live air quality (US AQI, PM2.5, PM10) and the UV index into one clear
verdict, such as **Good to go** or **Protect yourself**, plus a few short tips.

Built for **CP3406 / CP5307 Assessment 1: Utility App** with Kotlin, Jetpack Compose and Material Design 3.

---

## Core features

| Feature | What the user sees | How it is built |
|---|---|---|
| At-a-glance verdict | A large coloured card: *Good to go*, *Go out with care*, *Protect yourself* or *Best to stay indoors*, with 1 to 4 tips | `AirAdvisor` turns AQI + UV + sensitivity into an `Assessment`; `VerdictCard` composable |
| Haze card | US AQI number, colour-coded level, animated gauge, PM2.5 and PM10 | `MetricCard` + `LevelGauge` composables |
| UV card | Current UV index, WHO level, today's peak UV time, sun protection advice | `MetricCard`, `AirQualityReport.peakUvToday` |
| Hourly outlook | Scrollable chips for the next 6, 12 or 24 hours, each with a colour-coded AQI pill and UV value | `HourlyOutlookCard` with a `LazyRow` |
| Best time to head out | Highlights the best daytime hour (7 AM to 9 PM) in the outlook, ranked by verdict, then cleanest air, then weakest UV | `AirAdvisor.bestHourOutside()` on the hourly API data |
| City search | Type any city name and pick from results | Open-Meteo Geocoding API, debounced search in `HazeViewModel` |
| Quick picks | One-tap chips for Singapore, Seoul, Kuala Lumpur, Jakarta, Bangkok, Townsville | `FilterChip` in a `LazyRow` |
| Sensitivity | *Standard* or *Sensitive* (asthma, children, older adults, fair skin). Sensitive moves warnings one step earlier | `SegmentedButton`; advice is recalculated instantly without a new network call |
| Display options | Show or hide the Haze card, UV card and hourly outlook, and choose outlook length | `Switch` rows and segmented buttons that update `AppSettings` |
| Refresh and errors | Refresh button keeps old data visible while loading; friendly error card with *Try again*; if a refresh fails the last reading stays on screen with a short note | `AirUiState` (Loading / Success / Error), `isRefreshing` and `refreshError` flows |
| Auto refresh | Coming back to the app after 15+ minutes (or after an error) reloads the data by itself | `LifecycleResumeEffect` in `UtilityScreen` calls `refreshIfStale()` |
| Light and dark theme | Custom teal "clear sky" Material 3 colour scheme for both modes | `MobileTech_UtilityAppTheme` |
| App icon | Adaptive launcher icon of a sun behind haze lines | Vector drawables `ic_launcher_foreground.xml` / `ic_launcher_background.xml` |

Settings are kept in memory only and reset when the app closes, as allowed by the brief.

---

## Architecture

The app follows Google's recommended layered architecture (UI layer, data layer) with
unidirectional data flow: state flows **down** from the ViewModel to the composables and events flow **up** as lambdas.

```
UI layer        MainActivity / UtilityApp (Scaffold + bottom navigation)
                 ├── UtilityScreen   -> UtilityContent (stateless, previewable)
                 └── SettingsScreen  -> SettingsContent (stateless, previewable)
                          │  collectAsStateWithLifecycle()
                          ▼
                HazeViewModel  (StateFlow<AirUiState>, StateFlow<AppSettings>, StateFlow<SearchUiState>)
                          │
Domain          AirAdvisor  (pure Kotlin rules: AQI/UV bands -> verdict + tips)
                          │
Data layer      AirQualityRepository / LocationRepository   (interfaces)
                 └── Network...Repository  ->  Retrofit services  ->  Open-Meteo APIs
                          ▲
DI              AppContainer (manual dependency injection, created in UtilityApplication)
```

### Project structure

```
app/src/main/java/au/edu/jcu/assessment/utilityapp/
├── MainActivity.kt              Scaffold, bottom navigation, shared ViewModel
├── UtilityApplication.kt        Creates the DI container once
├── di/
│   └── AppContainer.kt          Builds OkHttp, Retrofit and repositories
├── data/
│   ├── model/
│   │   ├── AirQualityModels.kt  City, HourlyReading, AirQualityReport
│   │   └── Levels.kt            AirLevel, UvLevel, Sensitivity, Verdict, Assessment, AppSettings
│   ├── remote/
│   │   ├── NetworkModels.kt     @Serializable DTOs that mirror the API JSON
│   │   └── OpenMeteoServices.kt Retrofit interfaces
│   └── repository/
│       ├── AirQualityRepository.kt  Interface + network implementation + DTO mapping
│       └── LocationRepository.kt    Interface + network implementation
├── domain/
│   └── AirAdvisor.kt            Business rules (easy to unit test)
└── ui/
    ├── viewmodel/
    │   └── HazeViewModel.kt     UI state, settings, search, error handling
    ├── components/
    │   └── AirComponents.kt     VerdictCard, MetricCard, LevelGauge, HourlyOutlookCard, states
    ├── screens/
    │   ├── UtilityScreen.kt     Today screen + previews
    │   └── SettingsScreen.kt    Settings screen + preview
    └── theme/                   Colours, typography, Material 3 theme
```

---

## How the subject topics are used

| Week | Topic | Where it is used |
|---|---|---|
| 1 | Kotlin and Android Studio | Whole project: data classes, sealed interfaces, enums, extension functions, coroutines |
| 2 | Layouts with Jetpack Compose | `Column`, `Row`, `Box`, `BoxWithConstraints`, `LazyRow`, reusable composables in `ui/components`, stateless screen content with `@Preview`s in light and dark mode |
| 3 | Material Design 3 | Custom light/dark `ColorScheme`, type scale, `Card`, `FilterChip`, `SegmentedButton`, `Switch`, `ListItem`, `NavigationBar`, `FilledTonalIconButton` |
| 4 | App architecture | `HazeViewModel` with `StateFlow`, repository pattern with interfaces, manual DI through `AppContainer` and `ViewModelProvider.Factory`, lifecycle-aware collection (`collectAsStateWithLifecycle`) and refresh (`LifecycleResumeEffect`) |
| 5 | Web APIs with Retrofit | Two Retrofit services (Air Quality + Geocoding) using `kotlinx.serialization`, suspend functions and error handling |

---

## APIs

Both APIs are free and need no API key.

* **Open-Meteo Air Quality API**: `https://air-quality-api.open-meteo.com/v1/air-quality`
  (current and hourly `us_aqi`, `pm2_5`, `pm10`, `uv_index`, `timezone=auto`)
* **Open-Meteo Geocoding API**: `https://geocoding-api.open-meteo.com/v1/search`

Data by [Open-Meteo.com](https://open-meteo.com/) under CC BY 4.0.

---

## How to run

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync finish.
3. Run on an emulator or phone with **API 26+** and an internet connection.

### Tests

`app/src/test/.../HazeLogicTest.kt` contains 7 unit tests for the AQI and UV bands, the sensitivity rules,
the best-hour pick, and the mapping from the API response to the app model.
Right-click the file in Android Studio and choose **Run 'HazeLogicTest'**.

---

## Possible future improvements

* Save settings with DataStore so they persist between launches
* Use the phone's GPS location
* Daily notification when haze or UV is high
* Home screen widget with the verdict
