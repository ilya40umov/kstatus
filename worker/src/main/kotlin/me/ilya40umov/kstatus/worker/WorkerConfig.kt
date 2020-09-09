package me.ilya40umov.kstatus.worker

import me.ilya40umov.kstatus.conf.DatabaseConfig
import me.ilya40umov.kstatus.conf.KtorConfig
import me.ilya40umov.kstatus.sqs.SqsConfig
import me.ilya40umov.kstatus.worker.sitecheck.SiteCheckConfig

data class WorkerConfig(
    val metricsPort: Int,
    val ktor: KtorConfig = KtorConfig(),
    val database: DatabaseConfig,
    val sqs: SqsConfig,
    val siteCheck: SiteCheckConfig
)