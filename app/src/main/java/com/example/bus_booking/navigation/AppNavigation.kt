package com.example.bus_booking.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bus_booking.data.repository.AuthRepository
import com.example.bus_booking.ui.auth.LoginScreen
import com.example.bus_booking.ui.auth.RegisterScreen
import com.example.bus_booking.ui.booking.SeatSelectionScreen
import com.example.bus_booking.ui.mybookings.MyBookingsScreen
import com.example.bus_booking.ui.search.HomeSearchScreen
import com.example.bus_booking.ui.search.SearchResultsScreen
import com.example.bus_booking.ui.search.SearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val RESULTS = "results"
    const val SEATS = "seats/{tripId}"
    const val MY_BOOKINGS = "my_bookings"

    fun seatsRoute(tripId: Int) = "seats/$tripId"
}

@Composable
fun AppNavigation(
    authRepository: AuthRepository = koinInject()
) {
    val navController = rememberNavController()
    val savedToken by authRepository.tokenFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    val startDestination = if (!savedToken.isNullOrBlank()) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) { backStackEntry ->
            val sharedSearchViewModel: SearchViewModel =
                koinViewModel(viewModelStoreOwner = backStackEntry)
            HomeSearchScreen(
                viewModel = sharedSearchViewModel,
                onSearchComplete = {
                    navController.navigate(Routes.RESULTS)
                },
                onOpenMyBookings = {
                    navController.navigate(Routes.MY_BOOKINGS)
                },
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.RESULTS) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }
            val sharedSearchViewModel: SearchViewModel =
                koinViewModel(viewModelStoreOwner = parentEntry)
            SearchResultsScreen(
                viewModel = sharedSearchViewModel,
                onBack = { navController.popBackStack() },
                onSelectTrip = { tripId ->
                    navController.navigate(Routes.seatsRoute(tripId))
                }
            )
        }

        composable(
            route = Routes.SEATS,
            arguments = listOf(navArgument("tripId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: 0
            SeatSelectionScreen(
                tripId = tripId,
                onBack = { navController.popBackStack() },
                onBookingConfirmed = {
                    navController.navigate(Routes.MY_BOOKINGS) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(Routes.MY_BOOKINGS) {
            MyBookingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}