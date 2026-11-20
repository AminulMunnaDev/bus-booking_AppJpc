package com.example.bus_booking.data.remote

import com.example.bus_booking.data.model.BookingDto
import com.example.bus_booking.data.model.CreateBookingRequest
import com.example.bus_booking.data.model.PassengerRequest
import com.example.bus_booking.data.model.SeatDto
import com.example.bus_booking.data.model.TripDto
import com.example.bus_booking.data.model.TripSeatLayoutDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlin.math.abs

class BookingApi(
    private val client: HttpClient,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }
) {

    suspend fun getTripSeats(tripId: Int): TripSeatLayoutDto {
        val response = client.get("trips/$tripId")
        val raw = parseJsonOrThrow(response)
        return parseSeatLayout(tripId, raw)
    }

    suspend fun createBooking(request: CreateBookingRequest): BookingDto {
        val sanitizedPassengers = request.passengers.mapNotNull { p ->
            val trimmedName = p.name.trim()
            if (trimmedName.length < 2) return@mapNotNull null
            val cleanPhone = p.phone?.trim()?.takeIf { it.isNotEmpty() }
            val cleanGender = p.gender?.trim()?.uppercase()?.takeIf { it in setOf("M", "F", "O") }
            PassengerRequest(
                seatNumber = p.seatNumber.trim(),
                name = trimmedName,
                phone = cleanPhone,
                gender = cleanGender
            )
        }
        val cleanRequest = request.copy(passengers = sanitizedPassengers)

        val response = client.post("bookings") {
            contentType(ContentType.Application.Json)
            setBody(cleanRequest)
        }
        val raw = parseJsonOrThrow(response)
        return parseBooking(raw)
    }

    suspend fun getMyBookings(): List<BookingDto> {
        val response = client.get("bookings/my")
        val raw = parseJsonOrThrow(response)
        val array = extractArray(raw, "data", "bookings", "items") ?: return emptyList()
        return array.mapIndexedNotNull { index, item ->
            runCatching { parseBooking(item, fallbackIndex = index + 1) }.getOrNull()
        }
    }

    suspend fun getBookingById(bookingId: Int): BookingDto {
        val response = client.get("bookings/$bookingId")
        val raw = parseJsonOrThrow(response)
        return parseBooking(raw, fallbackId = bookingId)
    }

    suspend fun cancelBooking(bookingId: Int): BookingDto {
        val response = client.patch("bookings/$bookingId/cancel")
        val raw = parseJsonOrThrow(response)
        return parseBooking(raw, fallbackId = bookingId).copy(status = "CANCELLED", canCancel = false)
    }

    private suspend fun parseJsonOrThrow(response: HttpResponse): JsonElement {
        val text = response.bodyAsText()
        val parsed = runCatching { json.parseToJsonElement(text) }.getOrNull()

        if (!response.status.isSuccess()) {
            val rootObj = parsed as? JsonObject
            val primaryMsg = rootObj?.get("message").asStringOrNull()
            val firstValidationMsg = (rootObj?.get("errors") as? JsonArray)
                ?.firstOrNull()
                ?.let { it as? JsonObject }
                ?.get("message")
                .asStringOrNull()

            val combinedError = when {
                !primaryMsg.isNullOrBlank() && !firstValidationMsg.isNullOrBlank() && primaryMsg != firstValidationMsg ->
                    "$primaryMsg: $firstValidationMsg"
                !primaryMsg.isNullOrBlank() -> primaryMsg
                !firstValidationMsg.isNullOrBlank() -> firstValidationMsg
                text.isNotBlank() -> text
                else -> "Request failed (${response.status.value})"
            }
            error(combinedError)
        }

        return parsed ?: response.body()
    }

    private fun parseSeatLayout(tripId: Int, element: JsonElement): TripSeatLayoutDto {
        val root = element as? JsonObject ?: return TripSeatLayoutDto(tripId = tripId)
        val data = (root["data"] as? JsonObject) ?: root
        val routeObj = data["route"] as? JsonObject
        val busObj = data["bus"] as? JsonObject

        val seatLabels: List<String> = ((data["seatLayout"] as? JsonArray)
            ?: (data["seats"] as? JsonArray)
            ?: JsonArray(emptyList()))
            .mapNotNull { item ->
                when (item) {
                    is JsonPrimitive -> item.asStringOrNull()
                    is JsonObject -> item["seatNumber"].asStringOrNull() ?: item["label"].asStringOrNull()
                    else -> null
                }
            }

        val bookedLabels: Set<String> = ((data["bookedSeats"] as? JsonArray) ?: JsonArray(emptyList()))
            .mapNotNull { item ->
                when (item) {
                    is JsonPrimitive -> item.asStringOrNull()?.uppercase()
                    is JsonObject -> item["seatNumber"].asStringOrNull()?.uppercase()
                    else -> null
                }
            }
            .toSet()

        val seats = seatLabels.mapIndexed { index, label ->
            val isTaken = bookedLabels.contains(label.uppercase())
            SeatDto(
                id = index + 1,
                seatNumber = label,
                row = (index / 4) + 1,
                column = (index % 4) + 1,
                status = if (isTaken) "BOOKED" else "AVAILABLE",
                isBooked = isTaken
            )
        }

        val fare = data["price"].asIntOrNull()
            ?: data["fare"].asIntOrNull()
            ?: 0

        return TripSeatLayoutDto(
            tripId = data["id"].asIntOrNull() ?: tripId,
            fare = fare,
            price = fare,
            busName = busObj?.get("name").asStringOrNull(),
            busType = busObj?.get("type").asStringOrNull() ?: busObj?.get("busType").asStringOrNull(),
            fromCity = routeObj?.get("fromCity").asStringOrNull(),
            toCity = routeObj?.get("toCity").asStringOrNull(),
            departureTime = data["departureTime"].asStringOrNull(),
            arrivalTime = data["arrivalTime"].asStringOrNull(),
            travelDate = data["travelDate"].asStringOrNull(),
            totalSeats = busObj?.get("totalSeats").asIntOrNull() ?: seats.size,
            seatsLeft = data["seatsLeft"].asIntOrNull() ?: seats.count { it.isAvailable() },
            isBookable = data["isBookable"].asBooleanOrNull() ?: true,
            seats = seats
        )
    }

    private fun parseBooking(
        element: JsonElement,
        fallbackId: Int? = null,
        fallbackIndex: Int = 1
    ): BookingDto {
        val root = element as? JsonObject ?: error("Invalid booking response")
        val dataObj = (root["data"] as? JsonObject)
            ?: (root["booking"] as? JsonObject)
            ?: root

        val id = dataObj["id"].asStableIdOrNull()
            ?: fallbackId
            ?: run {
                val apiError = root["message"].asStringOrNull()
                if (apiError != null) error(apiError) else fallbackIndex
            }

        val seatsArray = (dataObj["seats"] as? JsonArray) ?: JsonArray(emptyList())
        val seatNumbers = seatsArray.mapNotNull { seatElem ->
            when (seatElem) {
                is JsonObject -> seatElem["seatNumber"].asStringOrNull()
                is JsonPrimitive -> seatElem.asStringOrNull()
                else -> null
            }
        }
        val firstPassengerName = seatsArray.firstOrNull()
            ?.let { it as? JsonObject }
            ?.get("passengerName")
            .asStringOrNull()
        val firstPassengerPhone = seatsArray.firstOrNull()
            ?.let { it as? JsonObject }
            ?.get("passengerPhone")
            .asStringOrNull()

        val tripObj = dataObj["trip"] as? JsonObject
        val tripDto = tripObj?.let { t ->
            val routeObj = t["route"] as? JsonObject
            val busObj = t["bus"] as? JsonObject
            val tripId = t["id"].asIntOrNull() ?: 0
            val price = t["price"].asIntOrNull() ?: t["fare"].asIntOrNull()
            TripDto(
                id = tripId,
                travelDate = t["travelDate"].asStringOrNull(),
                departureTime = t["departureTime"].asStringOrNull(),
                arrivalTime = t["arrivalTime"].asStringOrNull(),
                fare = price,
                price = price,
                busType = busObj?.get("type").asStringOrNull() ?: busObj?.get("busType").asStringOrNull(),
                busName = busObj?.get("name").asStringOrNull(),
                fromCity = routeObj?.get("fromCity").asStringOrNull(),
                toCity = routeObj?.get("toCity").asStringOrNull(),
                route = routeObj,
                bus = busObj
            )
        }

        return BookingDto(
            id = id,
            bookingCode = dataObj["bookingCode"].asStringOrNull() ?: "BK-$id",
            pnr = dataObj["pnr"].asStringOrNull() ?: "PNR-$id",
            status = dataObj["status"].asStringOrNull(),
            paymentStatus = dataObj["paymentStatus"].asStringOrNull(),
            totalAmount = dataObj["totalAmount"].asIntOrNull(),
            createdAt = dataObj["createdAt"].asStringOrNull(),
            canCancel = dataObj["canCancel"].asBooleanOrNull() ?: false,
            seatNumbers = seatNumbers,
            passengerName = firstPassengerName,
            passengerPhone = firstPassengerPhone,
            trip = tripDto
        )
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

    private fun JsonElement?.asBooleanOrNull(): Boolean? {
        return when (this) {
            null, is JsonNull -> null
            is JsonPrimitive -> booleanOrNull
            else -> null
        }
    }

    private fun JsonElement?.asStableIdOrNull(): Int? {
        return when (this) {
            null, is JsonNull -> null
            is JsonPrimitive -> {
                intOrNull ?: content.takeIf { it.isNotBlank() }?.let { rawId ->
                    val h = rawId.hashCode()
                    if (h == Int.MIN_VALUE) 1 else abs(h)
                }
            }
            else -> null
        }
    }
}