package com.example.bus_booking.data.remote


import com.example.bus_booking.data.model.ApiResponse
import com.example.bus_booking.data.model.AuthResponse
import com.example.bus_booking.data.model.LoginRequest
import com.example.bus_booking.data.model.RegisterRequest
import com.example.bus_booking.data.model.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(private val client: HttpClient) {

    suspend fun login(request: LoginRequest): AuthResponse {
        return client.post("auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        return client.post("auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun getMe(): ApiResponse<UserDto> {
        return client.get("auth/me").body()
    }
}