package com.example.bus_booking.data.remote


import com.example.bus_booking.data.model.CityDto
import com.example.bus_booking.data.model.PopularRouteDto
import com.example.bus_booking.data.model.TripDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

class TripApi(private val client: HttpClient) {

    suspend fun getCities(): List<CityDto> {
        val raw: JsonElement = client.get("cities").body()
        return extractCityList(raw)
    }

    suspend fun getPopularRoutes(): List<PopularRouteDto> {
        val raw: JsonElement = client.get("trips/popular-routes").body()
        return extractRouteList(raw)
    }

    suspend fun searchTrips(
        from: String,
        to: String,
        date: String,
        busType: String? = null
    ): List<TripDto> {
        val raw: JsonElement = client.get("trips/search") {
            parameter("from", from)
            parameter("to", to)
            parameter("date", date)
            if (!busType.isNullOrBlank() && busType != "ALL") {
                parameter("busType", busType)
            }
        }.body()
        return extractTripList(raw)
    }

    private fun extractCityList(element: JsonElement): List<CityDto> {
        val array = extractArray(element, "data", "cities", "items") ?: return emptyList()
        return array.mapIndexedNotNull { index, item ->
            when (item) {
                is JsonObject -> {
                    val name = item["name"].asStringOrNull()
                        ?: item["cityName"].asStringOrNull()
                        ?: item["title"].asStringOrNull()
                        ?: return@mapIndexedNotNull null
                    CityDto(
                        id = item["id"].asIntOrNull() ?: index + 1,
                        name = name
                    )
                }

                is JsonPrimitive -> {
                    val name = item.asStringOrNull()?.takeIf { it.isNotBlank() }
                        ?: return@mapIndexedNotNull null
                    CityDto(id = index + 1, name = name)
                }

                else -> null
            }
        }
    }

    private fun extractRouteList(element: JsonElement): List<PopularRouteDto> {
        val array = extractArray(element, "data", "routes", "popularRoutes", "items")
            ?: return emptyList()

        return array.mapIndexedNotNull { index, item ->
            val obj = item as? JsonObject ?: return@mapIndexedNotNull null
            val routeObj = obj["route"] as? JsonObject

            val origin = obj["fromCity"].asCityNameOrNull()
                ?: obj["from"].asCityNameOrNull()
                ?: obj["origin"].asCityNameOrNull()
                ?: routeObj?.get("fromCity").asCityNameOrNull()
                ?: routeObj?.get("from").asCityNameOrNull()
                ?: ""

            val destination = obj["toCity"].asCityNameOrNull()
                ?: obj["to"].asCityNameOrNull()
                ?: obj["destination"].asCityNameOrNull()
                ?: routeObj?.get("toCity").asCityNameOrNull()
                ?: routeObj?.get("to").asCityNameOrNull()
                ?: ""

            val price = obj["minPrice"].asIntOrNull()
                ?: obj["startingPrice"].asIntOrNull()
                ?: obj["price"].asIntOrNull()
                ?: obj["fare"].asIntOrNull()

            PopularRouteDto(
                id = obj["id"].asIntOrNull()
                    ?: obj["routeId"].asIntOrNull()
                    ?: index + 1,
                fromCity = origin.ifBlank { null },
                toCity = destination.ifBlank { null },
                from = origin.ifBlank { null },
                to = destination.ifBlank { null },
                minPrice = price,
                price = price,
                tripCount = obj["tripCount"].asIntOrNull() ?: obj["count"].asIntOrNull()
            )
        }
    }

    private fun extractTripList(element: JsonElement): List<TripDto> {
        val array = extractArray(element, "data", "trips", "results", "items") ?: return emptyList()

        return array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val id = obj["id"].asIntOrNull()
                ?: obj["tripId"].asIntOrNull()
                ?: return@mapNotNull null

            val busObj = obj["bus"] as? JsonObject
            val routeObj = obj["route"] as? JsonObject

            val fromCity = obj["fromCity"].asCityNameOrNull()
                ?: obj["from"].asCityNameOrNull()
                ?: routeObj?.get("fromCity").asCityNameOrNull()
                ?: routeObj?.get("from").asCityNameOrNull()

            val toCity = obj["toCity"].asCityNameOrNull()
                ?: obj["to"].asCityNameOrNull()
                ?: routeObj?.get("toCity").asCityNameOrNull()
                ?: routeObj?.get("to").asCityNameOrNull()

            val fare = obj["fare"].asIntOrNull()
                ?: obj["price"].asIntOrNull()
                ?: obj["ticketPrice"].asIntOrNull()
                ?: routeObj?.get("fare").asIntOrNull()
                ?: routeObj?.get("price").asIntOrNull()

            val seatsLeft = obj["seatsLeft"].asIntOrNull()
                ?: obj["availableSeats"].asIntOrNull()
                ?: obj["remainingSeats"].asIntOrNull()

            TripDto(
                id = id,
                travelDate = obj["travelDate"].asStringOrNull()
                    ?: obj["date"].asStringOrNull(),
                departureTime = obj["departureTime"].asStringOrNull(),
                arrivalTime = obj["arrivalTime"].asStringOrNull(),
                fare = fare,
                price = fare,
                seatsLeft = seatsLeft,
                busType = obj["busType"].asStringOrNull()
                    ?: busObj?.get("busType").asStringOrNull()
                    ?: busObj?.get("type").asStringOrNull(),
                busName = obj["busName"].asStringOrNull()
                    ?: busObj?.get("name").asStringOrNull()
                    ?: busObj?.get("busName").asStringOrNull(),
                busNumber = obj["busNumber"].asStringOrNull()
                    ?: busObj?.get("busNumber").asStringOrNull()
                    ?: busObj?.get("plateNumber").asStringOrNull(),
                fromCity = fromCity,
                toCity = toCity,
                route = routeObj,
                bus = busObj
            )
        }
    }

    private fun extractArray(element: JsonElement, vararg keys: String): JsonArray? {
        return when (element) {
            is JsonArray -> element
            is JsonObject -> {
                for (key in keys) {
                    val candidate = element[key]
                    if (candidate is JsonArray) return candidate
                    if (candidate is JsonObject) {
                        for (nestedKey in keys) {
                            val nested = candidate[nestedKey]
                            if (nested is JsonArray) return nested
                        }
                    }
                }
                null
            }

            else -> null
        }
    }

    private fun JsonElement?.asStringOrNull(): String? {
        return when (this) {
            null, is JsonNull -> null
            is JsonPrimitive -> content.takeUnless { it.equals("null", ignoreCase = true) }
            is JsonObject -> this["name"].asStringOrNull()
                ?: this["title"].asStringOrNull()
                ?: this["label"].asStringOrNull()

            is JsonArray -> null
        }
    }

    private fun JsonElement?.asCityNameOrNull(): String? {
        return when (this) {
            null, is JsonNull -> null
            is JsonPrimitive -> content.takeUnless { it.equals("null", ignoreCase = true) }
            is JsonObject -> this["name"].asStringOrNull()
                ?: this["cityName"].asStringOrNull()
                ?: this["title"].asStringOrNull()

            is JsonArray -> null
        }
    }

    private fun JsonElement?.asIntOrNull(): Int? {
        return when (this) {
            null, is JsonNull -> null
            is JsonPrimitive -> intOrNull ?: content.toDoubleOrNull()?.toInt()
            is JsonObject -> this["id"].asIntOrNull()
            is JsonArray -> null
        }
    }
}