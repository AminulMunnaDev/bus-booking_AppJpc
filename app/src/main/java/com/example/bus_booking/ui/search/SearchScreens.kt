package com.example.bus_booking.ui.search

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bus_booking.data.model.PopularRouteDto
import com.example.bus_booking.data.model.SortOption
import com.example.bus_booking.data.model.TripDto
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchScreen(
    onSearchComplete: () -> Unit,
    onOpenMyBookings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.cities.isEmpty() && uiState.popularRoutes.isEmpty() && !uiState.isLoadingHome) {
            viewModel.loadHomeData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Bus Trips") },
                actions = {
                    TextButton(onClick = onOpenMyBookings) { Text("My Bookings") }
                    TextButton(onClick = onLogout) { Text("Logout") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Where are you going?", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.fromCity,
                            onValueChange = viewModel::updateFromCity,
                            label = { Text("From City") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.toCity,
                            onValueChange = viewModel::updateToCity,
                            label = { Text("To City") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.travelDate,
                            onValueChange = viewModel::updateTravelDate,
                            label = { Text("Travel Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.cities.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Available cities: ${uiState.cities.joinToString { it.name }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        uiState.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.searchTrips(onSuccessNavigate = onSearchComplete) },
                            enabled = !uiState.isSearching,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Search Buses")
                            }
                        }
                    }
                }
            }

            if (uiState.isLoadingHome) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (uiState.popularRoutes.isNotEmpty()) {
                item {
                    Text("Popular Routes", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.popularRoutes) { route ->
                            PopularRouteCard(
                                route = route,
                                onClick = {
                                    viewModel.applyPopularRoute(route, onReadyToSearch = onSearchComplete)
                                }
                            )
                        }
                    }
                }
            } else if (!uiState.isLoadingHome && uiState.cities.isEmpty()) {
                item {
                    OutlinedButton(onClick = { viewModel.loadHomeData() }) {
                        Text("Reload Cities & Routes")
                    }
                }
            }
        }
    }
}

@Composable
private fun PopularRouteCard(route: PopularRouteDto, onClick: () -> Unit) {
    Card(modifier = Modifier.clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${route.origin()} → ${route.destination()}",
                fontWeight = FontWeight.SemiBold
            )
            route.displayPrice()?.let { price ->
                Text("From ৳$price", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    onBack: () -> Unit,
    onSelectTrip: (Int) -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.fromCity, uiState.toCity, uiState.travelDate) {
        if (
            !uiState.hasSearched &&
            !uiState.isSearching &&
            uiState.fromCity.isNotBlank() &&
            uiState.toCity.isNotBlank() &&
            uiState.travelDate.isNotBlank()
        ) {
            viewModel.searchTrips()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${uiState.fromCity} → ${uiState.toCity}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Date: ${uiState.travelDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "All", "AC" to "AC", "NON_AC" to "Non-AC").forEach { (key, label) ->
                    FilterChip(
                        selected = uiState.selectedBusType == key,
                        onClick = { viewModel.updateBusTypeFilter(key) },
                        label = { Text(label) }
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SortOption.entries) { option ->
                    FilterChip(
                        selected = uiState.sortOption == option,
                        onClick = { viewModel.updateSortOption(option) },
                        label = { Text(option.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isSearching -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.searchTrips() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.filteredTrips.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No trips found for this route/date.")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.searchTrips() }) {
                            Text("Retry Search")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.filteredTrips, key = { it.id }) { trip ->
                            TripResultCard(
                                trip = trip,
                                onSelect = { onSelectTrip(trip.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripResultCard(trip: TripDto, onSelect: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trip.busName ?: "Intercity Coach",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "৳${trip.unitPrice()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Departure: ${trip.departureTime ?: "--:--"}" +
                        (trip.arrivalTime?.let { "  •  Arrival: $it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trip.busType ?: "Standard"}  •  ${trip.seatsLeft ?: 0} seats left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onSelect,
                    enabled = (trip.seatsLeft ?: 0) > 0
                ) {
                    Text("Select Seats")
                }
            }
        }
    }
}