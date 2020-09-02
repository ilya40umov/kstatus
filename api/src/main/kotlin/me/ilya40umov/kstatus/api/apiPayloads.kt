package me.ilya40umov.kstatus.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val status: Int,
    val message: String
)