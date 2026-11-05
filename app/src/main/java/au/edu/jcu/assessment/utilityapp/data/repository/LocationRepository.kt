package au.edu.jcu.assessment.utilityapp.data.repository

import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.remote.GeocodingApiService
import au.edu.jcu.assessment.utilityapp.data.remote.GeocodingResult

/** Looks up cities by name so the user can check any place in the world. */
interface LocationRepository {
    suspend fun searchCities(query: String): List<City>
}

/** Uses the Open-Meteo Geocoding API to turn a typed name into coordinates. */
class NetworkLocationRepository(
    private val api: GeocodingApiService
) : LocationRepository {

    override suspend fun searchCities(query: String): List<City> =
        api.searchCities(name = query.trim()).results.map { it.toCity() }
}

internal fun GeocodingResult.toCity(): City = City(
    name = name,
    country = country,
    region = region,
    latitude = latitude,
    longitude = longitude
)
