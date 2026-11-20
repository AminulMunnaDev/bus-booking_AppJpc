package com.example.bus_booking.ui.mybookings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bus_booking.data.model.BookingDto
import com.example.bus_booking.ui.theme.Amber100
import com.example.bus_booking.ui.theme.Amber700
import com.example.bus_booking.ui.theme.Green100
import com.example.bus_booking.ui.theme.Green50
import com.example.bus_booking.ui.theme.Green600
import com.example.bus_booking.ui.theme.Green700
import com.example.bus_booking.ui.theme.Red100
import com.example.bus_booking.ui.theme.Red600
import com.example.bus_booking.ui.theme.Red700
import com.example.bus_booking.ui.theme.Slate200
import com.example.bus_booking.ui.theme.Slate300
import com.example.bus_booking.ui.theme.Slate50
import com.example.bus_booking.ui.theme.Slate500
import com.example.bus_booking.ui.theme.Slate600
import com.example.bus_booking.ui.theme.Slate900
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    viewModel: MyBookingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var bookingToCancel by remember { mutableStateOf<BookingDto?>(null) }
    var selectedTicket by remember { mutableStateOf<BookingDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    Scaffold(
        containerColor = Slate50,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Bookings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate900
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Slate900,
                    navigationIconContentColor = Slate900
                )
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = when {
                uiState.isLoading && uiState.bookings.isEmpty() -> "loading"
                uiState.bookings.isEmpty() -> "empty"
                else -> "list"
            },
            transitionSpec = {
                fadeIn(tween(240)) togetherWith fadeOut(tween(180))
            },
            label = "myBookingsState",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { screenMode ->
            when (screenMode) {
                "loading" -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green600)
                    }
                }

                "empty" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Slate200),
                            shadowElevation = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Green50)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = Green600,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (uiState.errorMessage != null) {
                                        "Couldn't load bookings"
                                    } else {
                                        "No tickets yet"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = uiState.errorMessage
                                        ?: "Your booked bus tickets will appear here once reserved.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (uiState.errorMessage != null) Red700 else Slate600,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = { viewModel.loadBookings() },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Slate300),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900)
                                ) {
                                    Text("Refresh", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            AnimatedVisibility(
                                visible = !uiState.errorMessage.isNullOrBlank(),
                                enter = fadeIn(tween(180)) + expandVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ),
                                exit = fadeOut(tween(150)) + shrinkVertically()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Red100,
                                    border = BorderStroke(1.dp, Red600.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = uiState.errorMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Red700,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        items(uiState.bookings, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                isCancelling = uiState.cancellingId == booking.id,
                                onViewTicket = { selectedTicket = booking },
                                onCancelRequest = { bookingToCancel = booking },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(220),
                                    fadeOutSpec = tween(180),
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Cancel confirmation dialog
    bookingToCancel?.let { target ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Cancel this booking?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            },
            text = {
                Text(
                    text = "Your reserved seats for Booking #${target.id} will be released immediately.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = target.id
                        bookingToCancel = null
                        viewModel.cancelBooking(id)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red600,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel Ticket")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { bookingToCancel = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate600)
                ) {
                    Text("Keep Ticket")
                }
            }
        )
    }

    // Ticket detail dialog
    selectedTicket?.let { ticket ->
        TicketDetailDialog(
            booking = ticket,
            onDismiss = { selectedTicket = null }
        )
    }
}

@Composable
private fun BookingCard(
    booking: BookingDto,
    isCancelling: Boolean,
    onViewTicket: () -> Unit,
    onCancelRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = booking.status.orEmpty().uppercase().ifBlank { "CONFIRMED" }
    val isCancelled = status == "CANCELLED"
    val canCancel = !isCancelled && (booking.canCancel || status == "CONFIRMED" || status == "PENDING")

    val trip = booking.trip
    val routeText = if (!trip?.fromCity.isNullOrBlank() && !trip?.toCity.isNullOrBlank()) {
        "${trip?.fromCity} → ${trip?.toCity}"
    } else {
        "Booking #${booking.id}"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate200),
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: Route + Status pill badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routeText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Text(
                        text = "Booking #${booking.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusPillBadge(status = status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            // Trip metadata
            trip?.busName?.let { bus ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsBus,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bus + (trip.busType?.let { " • $it" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (trip?.travelDate != null || trip?.departureTime != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = listOfNotNull(trip.travelDate, trip.departureTime).joinToString(" at "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            val seatsLabel = booking.seatNumbers?.takeIf { it.isNotEmpty() }?.joinToString()
                ?: if (isCancelled) "Released" else "—"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.EventSeat,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Seats: $seatsLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom row: Total amount + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    Text(
                        text = "৳${booking.totalAmount ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewTicket,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate300),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Slate900
                        )
                    ) {
                        Text(
                            text = "View Ticket",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (canCancel) {
                        CancelActionButton(
                            isCancelling = isCancelling,
                            onClick = onCancelRequest
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPillBadge(status: String) {
    val targetBg = when (status) {
        "CONFIRMED" -> Green100
        "PENDING" -> Amber100
        "CANCELLED" -> Red100
        else -> Green100
    }
    val targetText = when (status) {
        "CONFIRMED" -> Green700
        "PENDING" -> Amber700
        "CANCELLED" -> Red700
        else -> Green700
    }

    val bgColor by animateColorAsState(targetValue = targetBg, animationSpec = tween(220), label = "badgeBg")
    val textColor by animateColorAsState(targetValue = targetText, animationSpec = tween(220), label = "badgeText")

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bgColor
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CancelActionButton(
    isCancelling: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isCancelling) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cancelScale"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = !isCancelling,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Red600.copy(alpha = 0.45f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Red600
        ),
        modifier = Modifier.scale(scale)
    ) {
        if (isCancelling) {
            CircularProgressIndicator(
                color = Red600,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TicketDetailDialog(
    booking: BookingDto,
    onDismiss: () -> Unit
) {
    val status = booking.status.orEmpty().uppercase().ifBlank { "CONFIRMED" }
    val trip = booking.trip
    val routeText = if (!trip?.fromCity.isNullOrBlank() && !trip?.toCity.isNullOrBlank()) {
        "${trip?.fromCity} → ${trip?.toCity}"
    } else {
        "BusGo E-Ticket"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "E-Ticket",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                StatusPillBadge(status = status)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = routeText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(10.dp))

                TicketDetailRow("Booking ID", "#${booking.id}")
                trip?.busName?.let { TicketDetailRow("Coach", it) }
                trip?.travelDate?.let { TicketDetailRow("Date", it) }
                trip?.departureTime?.let { TicketDetailRow("Departure", it) }
                booking.passengerName?.let { TicketDetailRow("Passenger", it) }
                booking.seatNumbers?.takeIf { it.isNotEmpty() }?.let {
                    TicketDetailRow("Seats", it.joinToString())
                }
                TicketDetailRow(
                    label = "Total Fare",
                    value = "৳${booking.totalAmount ?: 0}",
                    valueColor = Green700,
                    bold = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green600,
                    contentColor = Color.White
                )
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun TicketDetailRow(
    label: String,
    value: String,
    valueColor: Color = Slate900,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}