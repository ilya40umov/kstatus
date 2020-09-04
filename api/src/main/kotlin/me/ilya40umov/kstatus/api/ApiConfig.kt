package me.ilya40umov.kstatus.api

import me.ilya40umov.kstatus.conf.DatabaseConfig
import me.ilya40umov.kstatus.conf.KtorConfig

data class ApiConfig(
    val apiPort: Int,
    val metricsPort: Int,
    val ktor: KtorConfig = KtorConfig(),
    val database: DatabaseConfig
)