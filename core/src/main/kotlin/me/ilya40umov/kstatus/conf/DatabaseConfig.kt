package me.ilya40umov.kstatus.conf

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder
import java.time.Duration

data class DatabaseConfig(
    val host: String,
    val port: Int = 3306,
    val database: String,
    val username: String,
    val password: String,
    val connectionTimeout: Duration = Duration.ofMillis(100)
)

fun DatabaseConfig.asPoolConfigBuilder(): ConnectionPoolConfigurationBuilder =
    ConnectionPoolConfigurationBuilder(
        host = host,
        port = port,
        username = username,
        password = password,
        database = database,
        connectionCreateTimeout = connectionTimeout.toMillis(),
        connectionTestTimeout = connectionTimeout.toMillis()
    )
