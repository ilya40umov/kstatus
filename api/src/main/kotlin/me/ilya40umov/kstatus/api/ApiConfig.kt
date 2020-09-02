package me.ilya40umov.kstatus.api

import me.ilya40umov.kstatus.conf.DatabaseConfig

data class ApiConfig(
    val ktor: KtorConfig,
    val database: DatabaseConfig
)

data class KtorConfig(
    val port: Int = 8080,
    val metricsPort: Int = 9090,
    val gracePeriodSeconds: Int = 1,
    val shutdownTimeoutSeconds: Int = 3
)