package com.example.bus_booking.di


import com.example.bus_booking.data.local.TokenManager
import com.example.bus_booking.data.remote.AuthApi
import com.example.bus_booking.data.remote.BookingApi
import com.example.bus_booking.data.remote.TripApi
import com.example.bus_booking.data.repository.AuthRepository
import com.example.bus_booking.data.repository.BookingRepository
import com.example.bus_booking.data.repository.TripRepository
import com.example.bus_booking.ui.auth.AuthViewModel
import com.example.bus_booking.ui.booking.BookingViewModel
import com.example.bus_booking.ui.mybookings.MyBookingsViewModel
import com.example.bus_booking.ui.search.SearchViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module



// Defaults to 10.0.2.2 for Android Emulator connecting to host localhost:5000
private const val BASE_API_URL = "http://192.168.0.109:5000/api/"

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            explicitNulls = false
        }
    }

    single { TokenManager(get()) }

    single {
        val tokenManager = get<TokenManager>()
        HttpClient(Android) {
            defaultRequest {
                url(BASE_API_URL)
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Accept, "application/json")

                // Read fresh token per request so logins/logouts take effect immediately
                val latestToken = runBlocking {
                    tokenManager.tokenFlow.firstOrNull()
                }
                if (!latestToken.isNullOrBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $latestToken")
                }
            }

            install(ContentNegotiation) {
                json(get<Json>())
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 1)
                exponentialDelay()
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("KtorClientLog", message)
                    }
                }
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
        }
    }

    // APIs & Repositories
    single { AuthApi(get()) }
    single { AuthRepository(get(), get()) }

    single { TripApi(get()) }
    single { TripRepository(get()) }

    single { BookingApi(get()) }
    single { BookingRepository(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { BookingViewModel(get()) }
    viewModel { MyBookingsViewModel(get()) }
}