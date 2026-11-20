package com.example.bus_booking.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavBackStackEntry
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

private const val SCREEN_TRANSITION_MS = 320
private const val SCREEN_FADE_OUT_MS = 220

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val RESULTS = "results"
    const val SEATS = "seats/{tripId}"
    const val MY_BOOKINGS = "my_bookings"

    fun seatsRoute(tripId: Int) = "seats/$tripId"
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.screenEnter() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(durationMillis = SCREEN_TRANSITION_MS, easing = FastOutSlowInEasing),
        initialOffset = { fullWidth -> fullWidth / 3 }
    ) + fadeIn(animationSpec = tween(durationMillis = SCREEN_TRANSITION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.screenExit() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(durationMillis = SCREEN_TRANSITION_MS, easing = FastOutSlowInEasing),
        targetOffset = { fullWidth -> -fullWidth / 6 }
    ) + fadeOut(animationSpec = tween(durationMillis = SCREEN_FADE_OUT_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.screenPopEnter() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(durationMillis = SCREEN_TRANSITION_MS, easing = FastOutSlowInEasing),
        initialOffset = { fullWidth -> -fullWidth / 6 }
    ) + fadeIn(animationSpec = tween(durationMillis = SCREEN_TRANSITION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.screenPopExit() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(durationMillis = SCREEN_TRANSITION_MS, easing = FastOutSlowInEasing),
        targetOffset = { fullWidth -> fullWidth / 3 }
    ) + fadeOut(animationSpec = tween(durationMillis = SCREEN_FADE_OUT_MS))

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
        startDestination = startDestination,
        enterTransition = { screenEnter() },
        exitTransition = { screenExit() },
        popEnterTransition = { screenPopEnter() },
        popExitTransition = { screenPopExit() }
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
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
                    navController.navigate(Routes.RESULTS) {
                        launchSingleTop = true
                    }
                },
                onOpenMyBookings = {
                    navController.navigate(Routes.MY_BOOKINGS) {
                        launchSingleTop = true
                    }
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
                    navController.navigate(Routes.seatsRoute(tripId)) {
                        launchSingleTop = true
                    }
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
                        launchSingleTop = true
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