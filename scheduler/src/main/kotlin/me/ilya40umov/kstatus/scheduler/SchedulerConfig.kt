package me.ilya40umov.kstatus.scheduler

import me.ilya40umov.kstatus.conf.DatabaseConfig
import me.ilya40umov.kstatus.conf.KtorConfig
import me.ilya40umov.kstatus.sqs.SqsConfig

data class SchedulerConfig(
    val metricsPort: Int,
    val ktor: KtorConfig = KtorConfig(),
    val database: DatabaseConfig,
    val sqs: SqsConfig
)