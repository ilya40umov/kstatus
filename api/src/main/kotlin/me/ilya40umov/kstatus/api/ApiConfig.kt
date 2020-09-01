package me.ilya40umov.kstatus.api

data class ApiConfig(
    val ktor: KtorConfig
)

data class KtorConfig(
    val port: Int = 8080,
    val metricsPort: Int = 9090,
    val gracePeriodSeconds: Int = 1,
    val shutdownTimeoutSeconds: Int = 5
)