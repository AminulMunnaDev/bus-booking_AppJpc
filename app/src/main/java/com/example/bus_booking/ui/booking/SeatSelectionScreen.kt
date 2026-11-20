package com.example.bus_booking.ui.booking


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bus_booking.data.model.SeatDto
import com.example.bus_booking.ui.theme.Green100
import com.example.bus_booking.ui.theme.Green50
import com.example.bus_booking.ui.theme.Green600
import com.example.bus_booking.ui.theme.Green700
import com.example.bus_booking.ui.theme.Red100
import com.example.bus_booking.ui.theme.Red600
import com.example.bus_booking.ui.theme.Red700
import com.example.bus_booking.ui.theme.Slate100
import com.example.bus_booking.ui.theme.Slate200
import com.example.bus_booking.ui.theme.Slate300
import com.example.bus_booking.ui.theme.Slate50
import com.example.bus_booking.ui.theme.Slate500
import com.example.bus_booking.ui.theme.Slate600
import com.example.bus_booking.ui.theme.Slate900
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

    val isConfirmed = uiState.isPaid && uiState.activeBooking != null
    val showBottomBar =
        !uiState.isLoadingSeats && !(uiState.layout == null && uiState.errorMessage != null) && !isConfirmed

    Scaffold(
        containerColor = Slate50,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isConfirmed) "Ticket Confirmed" else "Select Seats",
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
        },
        bottomBar = {
            if (showBottomBar) {
                SeatSelectionBottomBar(
                    selectedSeats = uiState.selectedSeats.sorted(),
                    totalFare = uiState.totalFare,
                    isSubmitting = uiState.isSubmitting,
                    enabled = !uiState.isSubmitting && uiState.selectedSeats.isNotEmpty(),
                    onConfirm = { viewModel.submitBookingAndPay() }
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = when {
                uiState.isLoadingSeats -> "loading"
                uiState.layout == null && uiState.errorMessage != null -> "fatal_error"
                isConfirmed -> "confirmed"
                else -> "selection"
            },
            transitionSpec = {
                fadeIn(tween(240)) togetherWith fadeOut(tween(180))
            },
            label = "seatScreenState",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
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

                "fatal_error" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BusGoCard {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "Failed to load trip details.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Red700,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedButton(
                                    onClick = { viewModel.loadTripSeats(tripId) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Slate300),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900)
                                ) {
                                    Text("Retry", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                "confirmed" -> {
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BusGoCard {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Green100)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Confirmed",
                                        tint = Green700,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Ticket Confirmed",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Green100
                                ) {
                                    Text(
                                        text = booking.status ?: "CONFIRMED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Green700,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Slate200)
                                Spacer(modifier = Modifier.height(12.dp))

                                SummaryRow("Booking ID", "#${booking.id}")
                                routeText?.let { SummaryRow("Route", it) }
                                booking.trip?.travelDate?.let { SummaryRow("Date", it) }
                                booking.trip?.departureTime?.let { SummaryRow("Departure", it) }
                                SummaryRow("Seats", confirmedSeats.joinToString())
                                SummaryRow(
                                    label = "Total Fare",
                                    value = "৳${booking.totalAmount ?: uiState.totalFare}",
                                    valueColor = Green700,
                                    boldValue = true
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                PrimaryActionButton(
                                    text = "View My Bookings",
                                    isLoading = false,
                                    enabled = true,
                                    onClick = onBookingConfirmed
                                )
                            }
                        }
                    }
                }

                else -> {
                    val layout = uiState.layout
                    val seats = layout?.seats.orEmpty()
                    val seatRows = seats.chunked(4)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Trip summary card
                        item {
                            BusGoCard {
                                val title = layout?.busName ?: "Trip #$tripId"
                                val route =
                                    if (!layout?.fromCity.isNullOrBlank() && !layout?.toCity.isNullOrBlank()) {
                                        "${layout?.fromCity} → ${layout?.toCity}"
                                    } else null

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Green50)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DirectionsBus,
                                                contentDescription = null,
                                                tint = Green600,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate900
                                            )
                                            route?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Slate600
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "৳${layout?.unitFare() ?: 0}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Green700
                                    )
                                }

                                layout?.travelDate?.let { d ->
                                    val dep = layout.departureTime?.let { " • $it" } ?: ""
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$d$dep",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                            }
                        }

                        // Seat grid card
                        item {
                            BusGoCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.EventSeat,
                                            contentDescription = null,
                                            tint = Green600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Select Seats",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate900
                                        )
                                    }
                                    Text(
                                        text = "Max 4",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                SeatLegendRow()
                                Spacer(modifier = Modifier.height(14.dp))

                                if (seatRows.isEmpty()) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = uiState.errorMessage
                                                ?: "No seat layout available for this trip.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (uiState.errorMessage != null) Red700 else Slate600,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.loadTripSeats(tripId) },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Slate300),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Slate900
                                            )
                                        ) {
                                            Text("Retry Loading Seats")
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        seatRows.forEach { rowSeats ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    rowSeats.take(2).forEach { seat ->
                                                        SeatChip(
                                                            seat = seat,
                                                            isSelected = uiState.selectedSeats.contains(
                                                                seat.seatNumber
                                                            ),
                                                            onClick = { viewModel.toggleSeat(seat) }
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(24.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    rowSeats.drop(2).forEach { seat ->
                                                        SeatChip(
                                                            seat = seat,
                                                            isSelected = uiState.selectedSeats.contains(
                                                                seat.seatNumber
                                                            ),
                                                            onClick = { viewModel.toggleSeat(seat) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Passenger details card
                        item {
                            BusGoCard {
                                Text(
                                    text = "Passenger Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = uiState.passengerName,
                                    onValueChange = viewModel::updatePassengerName,
                                    label = { Text("Passenger Name (min 2 chars)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = Slate500
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = seatFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = uiState.passengerPhone,
                                    onValueChange = viewModel::updatePassengerPhone,
                                    label = { Text("Phone Number (Optional)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Phone,
                                            contentDescription = null,
                                            tint = Slate500
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = seatFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                )
                            }
                        }

                        // Inline error banner
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
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 10.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatLegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(fill = Color.White, border = Slate300, label = "Available")
        LegendItem(fill = Green600, border = Green600, label = "Selected")
        LegendItem(fill = Slate300, border = Slate300, label = "Booked")
    }
}

@Composable
private fun LegendItem(
    fill: Color,
    border: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(fill)
                .border(1.dp, border, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
        )
    }
}

@Composable
private fun SeatChip(
    seat: SeatDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val available = seat.isAvailable()
    val interactionSource = remember(seat.seatNumber) { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            !available -> 1f
            isPressed -> 0.94f
            isSelected -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "seatScale"
    )

    val fillColor = when {
        !available -> Slate300
        isSelected -> Green600
        else -> Color.White
    }
    val borderColor = when {
        !available -> Slate300
        isSelected -> Green600
        else -> Slate300
    }
    val textColor = when {
        !available -> Slate500
        isSelected -> Color.White
        else -> Slate900
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(44.dp)
            .height(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(fillColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .then(
                if (available) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = seat.seatNumber,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun SeatSelectionBottomBar(
    selectedSeats: List<String>,
    totalFare: Int,
    isSubmitting: Boolean,
    enabled: Boolean,
    onConfirm: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = Slate200)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedSeats.isEmpty()) {
                            "No seats selected"
                        } else {
                            "Seats: ${selectedSeats.joinToString()}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                    Text(
                        text = "৳$totalFare",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                PrimaryActionButton(
                    text = "Confirm Booking (৳$totalFare)",
                    isLoading = isSubmitting,
                    enabled = enabled,
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "primaryButtonScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green600,
            contentColor = Color.White,
            disabledContainerColor = Slate300,
            disabledContentColor = Slate500
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .scale(scale)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BusGoCard(
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate200),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Slate900,
    boldValue: Boolean = false
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
            fontWeight = if (boldValue) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun seatFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Green600,
    unfocusedBorderColor = Slate300,
    focusedLabelColor = Green700,
    unfocusedLabelColor = Slate500,
    cursorColor = Green600,
    focusedTextColor = Slate900,
    unfocusedTextColor = Slate900,
)