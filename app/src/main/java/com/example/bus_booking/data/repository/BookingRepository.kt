package com.example.bus_booking.data.repository

import com.example.bus_booking.data.model.BookingDto
import com.example.bus_booking.data.model.CreateBookingRequest
import com.example.bus_booking.data.model.PassengerRequest
import com.example.bus_booking.data.model.TripSeatLayoutDto
import com.example.bus_booking.data.remote.BookingApi

class BookingRepository(
    private val bookingApi: BookingApi
) {
    suspend fun getTripSeats(tripId: Int): Result<TripSeatLayoutDto> = runCatching {
        bookingApi.getTripSeats(tripId)
    }

    suspend fun createBooking(
        tripId: Int,
        seatNumbers: List<String>,
        passengerName: String,
        passengerPhone: String? = null,
        gender: String? = null
    ): Result<BookingDto> = runCatching {
        val cleanName = passengerName.trim()
        val cleanPhone = passengerPhone?.trim()?.takeIf { it.isNotEmpty() }
        val cleanGender = gender?.trim()?.uppercase()?.takeIf { it in setOf("M", "F", "O") }

        val passengers = seatNumbers.map { seat ->
            PassengerRequest(
                seatNumber = seat.trim(),
                name = cleanName,
                phone = cleanPhone,
                gender = cleanGender
            )
        }

        bookingApi.createBooking(
            CreateBookingRequest(
                tripId = tripId,
                passengers = passengers
            )
        )
    }

    suspend fun getMyBookings(): Result<List<BookingDto>> = runCatching {
        bookingApi.getMyBookings()
    }

    suspend fun getBookingById(bookingId: Int): Result<BookingDto> = runCatching {
        bookingApi.getBookingById(bookingId)
    }

    suspend fun cancelBooking(bookingId: Int): Result<BookingDto> = runCatching {
        bookingApi.cancelBooking(bookingId)
    }
}