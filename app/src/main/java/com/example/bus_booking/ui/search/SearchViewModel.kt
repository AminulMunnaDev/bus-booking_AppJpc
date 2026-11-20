package com.example.bus_booking.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bus_booking.data.model.CityDto
import com.example.bus_booking.data.model.PopularRouteDto
import com.example.bus_booking.data.model.SortOption
import com.example.bus_booking.data.model.TripDto
import com.example.bus_booking.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class SearchUiState(
    val cities: List<CityDto> = emptyList(),
    val popularRoutes: List<PopularRouteDto> = emptyList(),
    val fromCity: String = "",
    val toCity: String = "",
    val travelDate: String = defaultDhakaDate(),
    val selectedBusType: String = "ALL",
    val sortOption: SortOption = SortOption.DEFAULT,
    val trips: List<TripDto> = emptyList(),
    val filteredTrips: List<TripDto> = emptyList(),
    val isLoadingHome: Boolean = false,
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        fun defaultDhakaDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Dhaka")
            }
            return sdf.format(Date())
        }
    }
}

class SearchViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHome = true, errorMessage = null) }

            val citiesResult = tripRepository.getCities()
            val routesResult = tripRepository.getPopularRoutes()

            val cities = citiesResult.getOrDefault(emptyList())
            val routes = routesResult.getOrDefault(emptyList())
            val firstError = citiesResult.exceptionOrNull()?.message
                ?: routesResult.exceptionOrNull()?.message

            _uiState.update {
                it.copy(
                    cities = cities,
                    popularRoutes = routes,
                    fromCity = it.fromCity.ifBlank { cities.firstOrNull()?.name.orEmpty() },
                    toCity = it.toCity.ifBlank { cities.getOrNull(1)?.name.orEmpty() },
                    isLoadingHome = false,
                    errorMessage = if (cities.isEmpty() && routes.isEmpty()) firstError else null
                )
            }
        }
    }

    fun updateFromCity(city: String) {
        _uiState.update { it.copy(fromCity = city, errorMessage = null) }
    }

    fun updateToCity(city: String) {
        _uiState.update { it.copy(toCity = city, errorMessage = null) }
    }

    fun updateTravelDate(date: String) {
        _uiState.update { it.copy(travelDate = date, errorMessage = null) }
    }

    fun applyPopularRoute(route: PopularRouteDto, onReadyToSearch: (() -> Unit)? = null) {
        val origin = route.origin()
        val dest = route.destination()
        if (origin.isNotBlank() && dest.isNotBlank()) {
            _uiState.update { it.copy(fromCity = origin, toCity = dest, errorMessage = null) }
            searchTrips(onSuccessNavigate = onReadyToSearch)
        }
    }

    fun searchTrips(onSuccessNavigate: (() -> Unit)? = null) {
        val current = _uiState.value
        if (current.fromCity.isBlank() || current.toCity.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select both From and To cities.") }
            return
        }
        if (current.fromCity.equals(current.toCity, ignoreCase = true)) {
            _uiState.update { it.copy(errorMessage = "Origin and destination cities must be different.") }
            return
        }
        if (current.travelDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a travel date (YYYY-MM-DD).") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null) }
            tripRepository.searchTrips(
                from = current.fromCity,
                to = current.toCity,
                date = current.travelDate
            ).onSuccess { trips ->
                val filtered = applyFilterAndSort(trips, current.selectedBusType, current.sortOption)
                _uiState.update {
                    it.copy(
                        trips = trips,
                        filteredTrips = filtered,
                        isSearching = false,
                        hasSearched = true,
                        errorMessage = null
                    )
                }
                onSuccessNavigate?.invoke()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        hasSearched = true,
                        errorMessage = throwable.message ?: "Failed to search trips"
                    )
                }
            }
        }
    }

    fun updateBusTypeFilter(busType: String) {
        _uiState.update { state ->
            state.copy(
                selectedBusType = busType,
                filteredTrips = applyFilterAndSort(state.trips, busType, state.sortOption)
            )
        }
    }

    fun updateSortOption(option: SortOption) {
        _uiState.update { state ->
            state.copy(
                sortOption = option,
                filteredTrips = applyFilterAndSort(state.trips, state.selectedBusType, option)
            )
        }
    }

    private fun applyFilterAndSort(
        trips: List<TripDto>,
        busType: String,
        sortOption: SortOption
    ): List<TripDto> {
        val filtered = when (busType.uppercase(Locale.US)) {
            "AC" -> trips.filter { it.busType?.contains("AC", ignoreCase = true) == true && !it.busType.contains("NON", ignoreCase = true) }
            "NON_AC" -> trips.filter { it.busType?.contains("NON", ignoreCase = true) == true }
            else -> trips
        }

        return when (sortOption) {
            SortOption.DEFAULT -> filtered
            SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.unitPrice() }
            SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.unitPrice() }
            SortOption.DEPARTURE_TIME -> filtered.sortedBy { it.departureTime.orEmpty() }
        }
    }


}