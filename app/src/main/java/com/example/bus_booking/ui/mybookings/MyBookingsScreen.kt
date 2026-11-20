package com.example.bus_booking.ui.mybookings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.bus_booking.data.model.BookingDto
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    viewModel: MyBookingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.bookings.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.bookings.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "You have no bookings yet.",
                        color = if (uiState.errorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.loadBookings() }) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.errorMessage?.let { err ->
                        item {
                            Text(err, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    items(uiState.bookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            isCancelling = uiState.cancellingId == booking.id,
                            onCancel = { viewModel.cancelBooking(booking.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: BookingDto,
    isCancelling: Boolean,
    onCancel: () -> Unit
) {
    val status = booking.status.orEmpty().uppercase()
    val isCancelled = status == "CANCELLED"
    val canCancel = !isCancelled && (booking.canCancel || status == "CONFIRMED" || status == "PENDING")

    val trip = booking.trip
    val routeText = if (!trip?.fromCity.isNullOrBlank() && !trip?.toCity.isNullOrBlank()) {
        "${trip?.fromCity} → ${trip?.toCity}"
    } else null

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = routeText ?: "Booking #${booking.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status.ifBlank { "CONFIRMED" },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isCancelled) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            trip?.busName?.let { bus ->
                Text("Bus: $bus", style = MaterialTheme.typography.bodyMedium)
            }
            if (trip?.travelDate != null || trip?.departureTime != null) {
                Text(
                    text = "Date: ${trip.travelDate.orEmpty()}  ${trip.departureTime.orEmpty()}".trim(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            booking.seatNumbers?.takeIf { it.isNotEmpty() }?.let { seats ->
                Text("Seats: ${seats.joinToString()}")
            }
            booking.totalAmount?.let { amount ->
                Text("Total: ৳$amount", fontWeight = FontWeight.Medium)
            }

            if (canCancel) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isCancelling,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Cancel Ticket")
                    }
                }
            }
        }
    }
}