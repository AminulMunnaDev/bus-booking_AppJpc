package com.example.bus_booking.data.model



import kotlinx.serialization.Serializable


@Serializable
data class SeatDto(
    val id: Int? = null,
    val seatNumber: String = "",
    val row: Int? = null,
    val column: Int? = null,
    val status: String = "AVAILABLE",
    val isBooked: Boolean = false
) {
    fun isAvailable(): Boolean =
        !isBooked && status.equals("AVAILABLE", ignoreCase = true)
}

@Serializable
data class TripSeatLayoutDto(
    val tripId: Int? = null,
    val fare: Int? = null,
    val price: Int? = null,
    val busName: String? = null,
    val busType: String? = null,
    val fromCity: String? = null,
    val toCity: String? = null,
    val departureTime: String? = null,
    val arrivalTime: String? = null,
    val travelDate: String? = null,
    val totalSeats: Int? = null,
    val seatsLeft: Int? = null,
    val isBookable: Boolean = true,
    val seats: List<SeatDto> = emptyList()
) {
    fun unitFare(): Int = price ?: fare ?: 0
}

@Serializable
data class PassengerRequest(
    val seatNumber: String,
    val name: String,
    val phone: String? = null,
    val gender: String? = null
)

@Serializable
data class CreateBookingRequest(
    val tripId: Int,
    val passengers: List<PassengerRequest>
)

@Serializable
data class BookingDto(
    val id: Int,
    val bookingCode: String? = null,
    val pnr: String? = null,
    val status: String? = null,
    val paymentStatus: String? = null,
    val totalAmount: Int? = null,
    val createdAt: String? = null,
    val canCancel: Boolean = false,
    val seatNumbers: List<String>? = null,
    val passengerName: String? = null,
    val passengerPhone: String? = null,
    val trip: TripDto? = null
)