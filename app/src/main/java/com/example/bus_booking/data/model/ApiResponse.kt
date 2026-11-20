package com.example.bus_booking.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean? = null,
    val message: String? = null,
    val data: T? = null
)