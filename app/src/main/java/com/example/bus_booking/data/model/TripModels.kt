package com.example.bus_booking.data.model


import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CityDto(
    val id: Int? = null,
    val name: String = ""
)

@Serializable
data class CitiesResponse(
    val data: List<CityDto>? = null,
    val cities: List<CityDto>? = null
) {
    fun resolvedList(): List<CityDto> = data ?: cities ?: emptyList()
}

@Serializable
data class PopularRouteDto(
    val id: Int? = null,
    val fromCity: String? = null,
    val toCity: String? = null,
    val from: String? = null,
    val to: String? = null,
    val minPrice: Int? = null,
    val price: Int? = null,
    val tripCount: Int? = null
) {
    fun origin(): String = fromCity ?: from ?: ""
    fun destination(): String = toCity ?: to ?: ""
    fun displayPrice(): Int? = minPrice ?: price
}

@Serializable
data class PopularRoutesResponse(
    val data: List<PopularRouteDto>? = null,
    val routes: List<PopularRouteDto>? = null
) {
    fun resolvedList(): List<PopularRouteDto> = data ?: routes ?: emptyList()
}

@Serializable
data class TripDto(
    val id: Int,
    val travelDate: String? = null,
    val departureTime: String? = null,
    val arrivalTime: String? = null,
    val fare: Int? = null,
    val price: Int? = null,
    val seatsLeft: Int? = null,
    val busType: String? = null,
    val busName: String? = null,
    val busNumber: String? = null,
    val fromCity: String? = null,
    val toCity: String? = null,
    val route: JsonElement? = null,
    val bus: JsonElement? = null
) {
    fun unitPrice(): Int = fare ?: price ?: 0
}

@Serializable
data class TripsSearchResponse(
    val data: List<TripDto>? = null,
    val trips: List<TripDto>? = null
) {
    fun resolvedList(): List<TripDto> = data ?: trips ?: emptyList()
}

enum class SortOption(val label: String) {
    DEFAULT("Default"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    DEPARTURE_TIME("Departure Time")
}