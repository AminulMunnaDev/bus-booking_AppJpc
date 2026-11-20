package com.example.bus_booking.ui.search

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bus_booking.data.model.PopularRouteDto
import com.example.bus_booking.data.model.SortOption
import com.example.bus_booking.data.model.TripDto
import com.example.bus_booking.ui.theme.Amber100
import com.example.bus_booking.ui.theme.Amber700
import com.example.bus_booking.ui.theme.Green100
import com.example.bus_booking.ui.theme.Green50
import com.example.bus_booking.ui.theme.Green600
import com.example.bus_booking.ui.theme.Green700
import com.example.bus_booking.ui.theme.Green800
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TextButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchScreen(
    onSearchComplete: () -> Unit,
    onOpenMyBookings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }
    var swapRotation by remember { mutableStateOf(0f) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val activeError = localValidationError ?: uiState.errorMessage

    val isSameCity = remember(uiState.fromCity, uiState.toCity) {
        val from = uiState.fromCity.trim()
        val to = uiState.toCity.trim()
        from.isNotEmpty() && to.isNotEmpty() && from.equals(to, ignoreCase = true)
    }

    fun validateAndSearch() {
        val from = uiState.fromCity.trim()
        val to = uiState.toCity.trim()
        if (from.isNotEmpty() && to.isNotEmpty() && from.equals(to, ignoreCase = true)) {
            val msg = "Origin and destination cannot be the same city ($from)."
            localValidationError = msg
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(msg)
            }
            return
        }
        localValidationError = null
        viewModel.searchTrips(onSuccessNavigate = onSearchComplete)
    }

    val animatedSwapRotation by animateFloatAsState(
        targetValue = swapRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swapRotation"
    )

    LaunchedEffect(Unit) {
        if (uiState.cities.isEmpty() && uiState.popularRoutes.isEmpty() && !uiState.isLoadingHome) {
            viewModel.loadHomeData()
        }
    }

    // Filtered city options pulled directly from backend uiState.cities
    val fromCityOptions = remember(uiState.cities, uiState.fromCity, uiState.toCity) {
        val query = uiState.fromCity.trim()
        val available = uiState.cities.filterNot {
            it.name.equals(uiState.toCity.trim(), ignoreCase = true)
        }
        if (query.isBlank() || available.any { it.name.equals(query, ignoreCase = true) }) {
            available
        } else {
            available.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    val toCityOptions = remember(uiState.cities, uiState.fromCity, uiState.toCity) {
        val query = uiState.toCity.trim()
        val available = uiState.cities.filterNot {
            it.name.equals(uiState.fromCity.trim(), ignoreCase = true)
        }
        if (query.isBlank() || available.any { it.name.equals(query, ignoreCase = true) }) {
            available
        } else {
            available.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    Scaffold(
        containerColor = Slate50,
        modifier = Modifier.imePadding()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Green header + overlapping white search card
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                            .background(Green600)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Green700)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DirectionsBus,
                                        contentDescription = "BusGo",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "BusGo",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Find your next intercity trip",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Green100
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = onOpenMyBookings) {
                                    Icon(
                                        imageVector = Icons.Outlined.ConfirmationNumber,
                                        contentDescription = "My Bookings",
                                        tint = Color.White
                                    )
                                }
                                IconButton(onClick = { showLogoutDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Logout,
                                        contentDescription = "Logout",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Overlapping search card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Slate200),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 92.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Search Buses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Green50,
                                    border = BorderStroke(1.dp, Green100),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            swapRotation += 180f
                                            val oldFrom = uiState.fromCity
                                            val oldTo = uiState.toCity
                                            viewModel.updateFromCity(oldTo)
                                            viewModel.updateToCity(oldFrom)
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.SwapVert,
                                            contentDescription = "Swap cities",
                                            tint = Green700,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .rotate(animatedSwapRotation)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // From City Dropdown (populated from backend cities)
                            ExposedDropdownMenuBox(
                                expanded = fromExpanded,
                                onExpandedChange = { fromExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = uiState.fromCity,
                                    onValueChange = {
                                        viewModel.updateFromCity(it)
                                        fromExpanded = true
                                    },
                                    label = { Text("From City") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationOn,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = searchFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                                        .fillMaxWidth()
                                )

                                if (fromCityOptions.isNotEmpty()) {
                                    ExposedDropdownMenu(
                                        expanded = fromExpanded,
                                        onDismissRequest = { fromExpanded = false },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Slate200)
                                    ) {
                                        fromCityOptions.forEach { city ->
                                            val isCurrent = city.name.equals(uiState.fromCity, ignoreCase = true)
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = city.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                                        color = if (isCurrent) Green700 else Slate900
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.updateFromCity(city.name)
                                                    fromExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // To City Dropdown (populated from backend cities)
                            ExposedDropdownMenuBox(
                                expanded = toExpanded,
                                onExpandedChange = { toExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = uiState.toCity,
                                    onValueChange = {
                                        viewModel.updateToCity(it)
                                        toExpanded = true
                                    },
                                    label = { Text("To City") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationOn,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = searchFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                                        .fillMaxWidth()
                                )

                                if (toCityOptions.isNotEmpty()) {
                                    ExposedDropdownMenu(
                                        expanded = toExpanded,
                                        onDismissRequest = { toExpanded = false },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Slate200)
                                    ) {
                                        toCityOptions.forEach { city ->
                                            val isCurrent = city.name.equals(uiState.toCity, ignoreCase = true)
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = city.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                                        color = if (isCurrent) Green700 else Slate900
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.updateToCity(city.name)
                                                    toExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = uiState.travelDate,
                                onValueChange = viewModel::updateTravelDate,
                                label = { Text("Travel Date (YYYY-MM-DD)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = searchFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
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

                            Spacer(modifier = Modifier.height(14.dp))

                            SearchPrimaryButton(
                                text = "Search Buses",
                                isLoading = uiState.isSearching,
                                enabled = !uiState.isSearching,
                                onClick = { viewModel.searchTrips(onSuccessNavigate = onSearchComplete) }
                            )
                        }
                    }
                }
            }

            if (uiState.isLoadingHome) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green600)
                    }
                }
            }

            if (uiState.popularRoutes.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Route,
                                contentDescription = null,
                                tint = Green600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Popular Routes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.popularRoutes) { route ->
                                PopularRouteCard(
                                    route = route,
                                    onClick = {
                                        viewModel.applyPopularRoute(
                                            route,
                                            onReadyToSearch = onSearchComplete
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            } else if (!uiState.isLoadingHome && uiState.cities.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.loadHomeData() },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Slate300),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900)
                        ) {
                            Text("Reload Cities & Routes", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Confirm Logout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of your BusGo account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red600,
                        contentColor = Color.White
                    )
                ) {
                    Text("Logout", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate600)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
private fun PopularRouteCard(route: PopularRouteDto, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "routeCardScale"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate200),
        shadowElevation = 0.dp,
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${route.origin()} → ${route.destination()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate900
            )
            route.displayPrice()?.let { price ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "From ৳$price",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Green700
                )
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
        containerColor = Slate50,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${uiState.fromCity} → ${uiState.toCity}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = uiState.travelDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter & sort bar
            Surface(
                color = Color.White,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "ALL" to "All",
                            "AC" to "AC",
                            "NON_AC" to "Non-AC"
                        ).forEach { (key, label) ->
                            val selected = uiState.selectedBusType == key
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.updateBusTypeFilter(key) },
                                shape = RoundedCornerShape(999.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    labelColor = Slate600,
                                    selectedContainerColor = Green100,
                                    selectedLabelColor = Green800
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = Slate300,
                                    selectedBorderColor = Green600.copy(alpha = 0.35f)
                                ),
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SortOption.entries) { option ->
                            val selected = uiState.sortOption == option
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.updateSortOption(option) },
                                shape = RoundedCornerShape(999.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Slate50,
                                    labelColor = Slate600,
                                    selectedContainerColor = Green600,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = Slate200,
                                    selectedBorderColor = Green600
                                ),
                                label = {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Slate200)

            AnimatedContent(
                targetState = when {
                    uiState.isSearching -> "loading"
                    uiState.errorMessage != null -> "error"
                    uiState.filteredTrips.isEmpty() -> "empty"
                    else -> "results"
                },
                transitionSpec = {
                    fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                },
                label = "searchResultsState",
                modifier = Modifier.fillMaxSize()
            ) { stateMode ->
                when (stateMode) {
                    "loading" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Green600)
                        }
                    }

                    "error" -> {
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
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = uiState.errorMessage ?: "Search failed.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Red700,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.searchTrips() },
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
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No trips found for this route/date.",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.searchTrips() },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Slate300),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900)
                                    ) {
                                        Text("Retry Search", fontWeight = FontWeight.Medium)
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
                                Text(
                                    text = "${uiState.filteredTrips.size} ${if (uiState.filteredTrips.size == 1) "bus" else "buses"} available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }

                            items(uiState.filteredTrips, key = { it.id }) { trip ->
                                TripResultCard(
                                    trip = trip,
                                    onSelect = { onSelectTrip(trip.id) },
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
    }
}

@Composable
private fun TripResultCard(
    trip: TripDto,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val seatsLeft = trip.seatsLeft ?: 0
    val isBookable = seatsLeft > 0

    val seatsTextColor = when {
        !isBookable || seatsLeft <= 5 -> Red600
        seatsLeft <= 10 -> Amber700
        else -> Slate500
    }

    val badgeBg = when {
        trip.busType.orEmpty().contains("AC", ignoreCase = true) &&
                !trip.busType.orEmpty().contains("NON", ignoreCase = true) -> Green100

        seatsLeft in 1..5 -> Amber100
        else -> Slate100
    }
    val badgeTextColor = when {
        trip.busType.orEmpty().contains("AC", ignoreCase = true) &&
                !trip.busType.orEmpty().contains("NON", ignoreCase = true) -> Green700

        seatsLeft in 1..5 -> Amber700
        else -> Slate600
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = trip.busName ?: "Intercity Coach",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = trip.busType ?: "Standard",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Dep: ${trip.departureTime ?: "--:--"}" +
                                    (trip.arrivalTime?.let { " • Arr: $it" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳${trip.unitPrice()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Green700
                    )
                    Text(
                        text = "per seat",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBookable) "$seatsLeft seats left" else "Sold out",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (seatsLeft <= 5) FontWeight.SemiBold else FontWeight.Normal,
                    color = seatsTextColor
                )

                Button(
                    onClick = onSelect,
                    enabled = isBookable,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600,
                        contentColor = Color.White,
                        disabledContainerColor = Slate300,
                        disabledContentColor = Slate500
                    )
                ) {
                    Text(
                        text = "Select Seats",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchPrimaryButton(
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
        label = "searchButtonScale"
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
private fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Slate50,
    focusedBorderColor = Green600,
    unfocusedBorderColor = Slate300,
    disabledBorderColor = Slate200,
    focusedLabelColor = Green700,
    unfocusedLabelColor = Slate500,
    focusedLeadingIconColor = Green600,
    unfocusedLeadingIconColor = Slate500,
    cursorColor = Green600,
    focusedTextColor = Slate900,
    unfocusedTextColor = Slate900,
    focusedTrailingIconColor = Slate600,
    unfocusedTrailingIconColor = Slate600
)