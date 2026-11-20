package com.example.bus_booking.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bus_booking.data.model.SeatDto
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    tripId: Int,
    onBack: () -> Unit,
    onBookingConfirmed: () -> Unit,
    viewModel: BookingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tripId) {
        viewModel.loadTripSeats(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Seats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoadingSeats -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.layout == null && uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Failed to load trip details.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.loadTripSeats(tripId) }) {
                        Text("Retry")
                    }
                }
            }

            uiState.isPaid && uiState.activeBooking != null -> {
                val booking = uiState.activeBooking!!
                val confirmedSeats = booking.seatNumbers?.takeIf { it.isNotEmpty() }
                    ?: uiState.selectedSeats.sorted()
                val routeText = booking.trip?.let { t ->
                    val from = t.fromCity.orEmpty()
                    val to = t.toCity.orEmpty()
                    if (from.isNotBlank() && to.isNotBlank()) "$from → $to" else null
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ticket Confirmed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Booking ID: #${booking.id}", fontWeight = FontWeight.SemiBold)
                    Text("Status: ${booking.status ?: "CONFIRMED"}")
                    routeText?.let { Text("Route: $it") }
                    booking.trip?.travelDate?.let { Text("Date: $it") }
                    booking.trip?.departureTime?.let { Text("Departure: $it") }
                    Text("Seats: ${confirmedSeats.joinToString()}")
                    Text("Total Fare: ৳${booking.totalAmount ?: uiState.totalFare}")
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBookingConfirmed, modifier = Modifier.fillMaxWidth()) {
                        Text("View My Bookings")
                    }
                }
            }

            else -> {
                val layout = uiState.layout
                val seats = layout?.seats.orEmpty()
                val seatRows = seats.chunked(4)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                val title = layout?.busName ?: "Trip #$tripId"
                                val route = if (!layout?.fromCity.isNullOrBlank() && !layout?.toCity.isNullOrBlank()) {
                                    "${layout?.fromCity} → ${layout?.toCity}"
                                } else null

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                route?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    text = "Fare per seat: ৳${layout?.unitFare() ?: 0}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                layout?.travelDate?.let { d ->
                                    val dep = layout.departureTime?.let { " at $it" } ?: ""
                                    Text("Schedule: $d$dep", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    item {
                        Text("Select Seats (max 4):", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (seatRows.isEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = uiState.errorMessage ?: "No seat layout available for this trip.",
                                    color = if (uiState.errorMessage != null) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { viewModel.loadTripSeats(tripId) }) {
                                    Text("Retry Loading Seats")
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                seatRows.forEach { rowSeats ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            rowSeats.take(2).forEach { seat ->
                                                SeatChip(
                                                    seat = seat,
                                                    isSelected = uiState.selectedSeats.contains(seat.seatNumber),
                                                    onClick = { viewModel.toggleSeat(seat) }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            rowSeats.drop(2).forEach { seat ->
                                                SeatChip(
                                                    seat = seat,
                                                    isSelected = uiState.selectedSeats.contains(seat.seatNumber),
                                                    onClick = { viewModel.toggleSeat(seat) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Passenger Details", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = uiState.passengerName,
                                    onValueChange = viewModel::updatePassengerName,
                                    label = { Text("Passenger Name (min 2 chars)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = uiState.passengerPhone,
                                    onValueChange = viewModel::updatePassengerPhone,
                                    label = { Text("Phone Number (Optional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    uiState.errorMessage?.let { err ->
                        item {
                            Text(err, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    item {
                        Text(
                            text = "Selected: ${uiState.selectedSeats.sorted().joinToString().ifBlank { "None" }}  •  Total: ৳${uiState.totalFare}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.submitBookingAndPay() },
                            enabled = !uiState.isSubmitting && uiState.selectedSeats.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Confirm Booking (৳${uiState.totalFare})")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatChip(
    seat: SeatDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val available = seat.isAvailable()
    when {
        !available -> {
            OutlinedButton(
                onClick = {},
                enabled = false,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(seat.seatNumber)
            }
        }
        isSelected -> {
            Button(
                onClick = onClick,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(seat.seatNumber)
            }
        }
        else -> {
            OutlinedButton(
                onClick = onClick,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(seat.seatNumber)
            }
        }
    }
}