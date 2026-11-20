package com.example.bus_booking.data.model


import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val password: String
)

@Serializable
data class UserDto(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null
)

@Serializable
data class AuthData(
    val token: String? = null,
    val user: UserDto? = null
)

@Serializable
data class AuthResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val token: String? = null,
    val user: UserDto? = null,
    val data: AuthData? = null
) {
    fun resolvedToken(): String? = token ?: data?.token
    fun resolvedUser(): UserDto? = user ?: data?.user
}