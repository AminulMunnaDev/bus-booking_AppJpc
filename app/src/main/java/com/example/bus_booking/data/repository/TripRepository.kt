package com.example.bus_booking.data.repository



import com.example.bus_booking.data.model.CityDto
import com.example.bus_booking.data.model.PopularRouteDto
import com.example.bus_booking.data.model.TripDto
import com.example.bus_booking.data.remote.TripApi

class TripRepository(
    private val tripApi: TripApi
) {
    suspend fun getCities(): Result<List<CityDto>> = runCatching {
        tripApi.getCities()
    }

    suspend fun getPopularRoutes(): Result<List<PopularRouteDto>> = runCatching {
        tripApi.getPopularRoutes()
    }

    suspend fun searchTrips(
        from: String,
        to: String,
        date: String,
        busType: String? = null
    ): Result<List<TripDto>> = runCatching {
        tripApi.searchTrips(
            from = from.trim(),
            to = to.trim(),
            date = date.trim(),
            busType = busType
        )
    }
}