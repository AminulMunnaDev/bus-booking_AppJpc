package com.example.bus_booking.ui.mybookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bus_booking.data.model.BookingDto
import com.example.bus_booking.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyBookingsUiState(
    val bookings: List<BookingDto> = emptyList(),
    val isLoading: Boolean = false,
    val cancellingId: Int? = null,
    val errorMessage: String? = null
)

class MyBookingsViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingRepository.getMyBookings()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            bookings = list,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load bookings"
                        )
                    }
                }
        }
    }

    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cancellingId = bookingId, errorMessage = null) }
            bookingRepository.cancelBooking(bookingId)
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(
                            cancellingId = null,
                            bookings = state.bookings.map { item ->
                                if (item.id == bookingId) {
                                    updated.copy(status = updated.status ?: "CANCELLED")
                                } else {
                                    item
                                }
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            cancellingId = null,
                            errorMessage = throwable.message ?: "Could not cancel booking"
                        )
                    }
                }
        }
    }
}