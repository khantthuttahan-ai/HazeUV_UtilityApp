package au.edu.jcu.assessment.utilityapp.di

import au.edu.jcu.assessment.utilityapp.data.remote.AirQualityApiService
import au.edu.jcu.assessment.utilityapp.data.remote.GeocodingApiService
import au.edu.jcu.assessment.utilityapp.data.repository.AirQualityRepository
import au.edu.jcu.assessment.utilityapp.data.repository.LocationRepository
import au.edu.jcu.assessment.utilityapp.data.repository.NetworkAirQualityRepository
import au.edu.jcu.assessment.utilityapp.data.repository.NetworkLocationRepository
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Manual dependency injection container.
 * It builds every dependency once and hands them to whoever needs them,
 * so classes never create their own Retrofit or repository instances.
 */
interface AppContainer {
    val airQualityRepository: AirQualityRepository
    val locationRepository: LocationRepository
}

class DefaultAppContainer : AppContainer {

    // Ignore JSON fields we don't use, so API additions never break parsing
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // One shared HTTP client for both APIs
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun buildRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val airQualityService: AirQualityApiService by lazy {
        buildRetrofit(AirQualityApiService.BASE_URL).create(AirQualityApiService::class.java)
    }

    private val geocodingService: GeocodingApiService by lazy {
        buildRetrofit(GeocodingApiService.BASE_URL).create(GeocodingApiService::class.java)
    }

    override val airQualityRepository: AirQualityRepository by lazy {
        NetworkAirQualityRepository(airQualityService)
    }

    override val locationRepository: LocationRepository by lazy {
        NetworkLocationRepository(geocodingService)
    }
}
