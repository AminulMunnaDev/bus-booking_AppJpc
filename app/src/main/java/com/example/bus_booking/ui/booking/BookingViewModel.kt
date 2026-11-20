package com.example.bus_booking.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bus_booking.data.model.BookingDto
import com.example.bus_booking.data.model.SeatDto
import com.example.bus_booking.data.model.TripSeatLayoutDto
import com.example.bus_booking.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeatSelectionUiState(
    val tripId: Int = 0,
    val layout: TripSeatLayoutDto? = null,
    val selectedSeats: Set<String> = emptySet(),
    val passengerName: String = "",
    val passengerPhone: String = "",
    val passengerEmail: String = "",
    val gender: String? = null,
    val paymentMethod: String = "BKASH",
    val isLoadingSeats: Boolean = false,
    val isSubmitting: Boolean = false,
    val activeBooking: BookingDto? = null,
    val isPaid: Boolean = false,
    val errorMessage: String? = null
) {
    val totalFare: Int
        get() = (layout?.unitFare() ?: 0) * selectedSeats.size
}

class BookingViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeatSelectionUiState())
    val uiState: StateFlow<SeatSelectionUiState> = _uiState.asStateFlow()

    fun loadTripSeats(tripId: Int) {
        viewModelScope.launch {
            _uiState.update { current ->
                SeatSelectionUiState(
                    tripId = tripId,
                    passengerName = current.passengerName,
                    passengerPhone = current.passengerPhone,
                    passengerEmail = current.passengerEmail,
                    isLoadingSeats = true
                )
            }
            bookingRepository.getTripSeats(tripId)
                .onSuccess { layout ->
                    _uiState.update {
                        it.copy(
                            layout = layout,
                            isLoadingSeats = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingSeats = false,
                            errorMessage = throwable.message ?: "Failed to load seats"
                        )
                    }
                }
        }
    }

    fun toggleSeat(seat: SeatDto) {
        if (!seat.isAvailable()) return
        _uiState.update { state ->
            val updated = state.selectedSeats.toMutableSet()
            if (updated.contains(seat.seatNumber)) {
                updated.remove(seat.seatNumber)
            } else {
                if (updated.size >= 4) {
                    return@update state.copy(errorMessage = "Maximum 4 seats per booking.")
                }
                updated.add(seat.seatNumber)
            }
            state.copy(selectedSeats = updated, errorMessage = null)
        }
    }

    fun updatePassengerName(name: String) {
        _uiState.update { it.copy(passengerName = name, errorMessage = null) }
    }

    fun updatePassengerPhone(phone: String) {
        _uiState.update { it.copy(passengerPhone = phone, errorMessage = null) }
    }

    fun updatePassengerEmail(email: String) {
        _uiState.update { it.copy(passengerEmail = email, errorMessage = null) }
    }

    fun updateGender(gender: String?) {
        _uiState.update { it.copy(gender = gender, errorMessage = null) }
    }

    fun updatePaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun submitBookingAndPay() {
        val state = _uiState.value
        if (state.selectedSeats.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Select at least one seat.") }
            return
        }
        if (state.passengerName.trim().length < 2) {
            _uiState.update { it.copy(errorMessage = "Passenger name must be at least 2 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            bookingRepository.createBooking(
                tripId = state.tripId,
                seatNumbers = state.selectedSeats.toList().sorted(),
                passengerName = state.passengerName,
                passengerPhone = state.passengerPhone,
                gender = state.gender
            ).onSuccess { booking ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        activeBooking = booking,
                        isPaid = true,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "Could not complete booking"
                    )
                }
            }
        }
    }
}