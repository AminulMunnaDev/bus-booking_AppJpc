package com.example.bus_booking.data.repository

import com.example.bus_booking.data.local.TokenManager
import com.example.bus_booking.data.model.LoginRequest
import com.example.bus_booking.data.model.RegisterRequest
import com.example.bus_booking.data.model.UserDto
import com.example.bus_booking.data.remote.AuthApi
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    val tokenFlow: Flow<String?> = tokenManager.tokenFlow

    suspend fun login(email: String, password: String): Result<UserDto?> {
        return runCatching {
            val response = authApi.login(
                LoginRequest(
                    email = email.trim().lowercase(),
                    password = password
                )
            )
            val token = response.resolvedToken()
            if (!token.isNullOrBlank()) {
                tokenManager.saveToken(token)
            } else {
                error(response.message ?: "Login failed: token missing")
            }
            response.resolvedUser()
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<UserDto?> {
        return runCatching {
            val response = authApi.register(
                RegisterRequest(
                    name = name.trim(),
                    email = email.trim().lowercase(),
                    phone = phone.trim().ifBlank { null },
                    password = password
                )
            )
            val token = response.resolvedToken()
            if (!token.isNullOrBlank()) {
                tokenManager.saveToken(token)
            }
            response.resolvedUser()
        }
    }

    suspend fun getCurrentUser(): Result<UserDto?> {
        return runCatching {
            val response = authApi.getMe()
            response.data
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}