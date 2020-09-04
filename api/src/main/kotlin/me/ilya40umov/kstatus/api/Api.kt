package me.ilya40umov.kstatus.api

import com.github.jasync.sql.db.mysql.MySQLConnection
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder
import com.github.jasync.sql.db.pool.ConnectionPool
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import me.ilya40umov.kstatus.api.ktor.withErrorHandling
import me.ilya40umov.kstatus.api.routes.apiV1Sites
import me.ilya40umov.kstatus.conf.asPoolConfigBuilder
import me.ilya40umov.kstatus.conf.loadConfig
import me.ilya40umov.kstatus.health.withHealthEndpoint
import me.ilya40umov.kstatus.ktor.ktorEngine
import me.ilya40umov.kstatus.ktor.withBaseApiModules
import me.ilya40umov.kstatus.metrics.withMetricsModule
import me.ilya40umov.kstatus.site.SiteRepository
import me.ilya40umov.kstatus.site.SiteService
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

private val logger = KotlinLogging.logger {}

@KtorExperimentalAPI
fun main() {
    logger.info { "Starting Api..." }
    val apiConfig = loadConfig<ApiConfig>()
    logger.info { "Configuration loaded." }
    val di = createDi(apiConfig)
    Api(di).start()
}

private fun createDi(apiConfig: ApiConfig) = DI {
    bind<ApiConfig>() with instance(apiConfig)
    bind<MeterRegistry>() with singleton { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
    bind<ConnectionPool<MySQLConnection>>() with singleton {
        val config = apiConfig.database.asPoolConfigBuilder().build()
        MySQLConnectionBuilder.createConnectionPool(config)
    }
    bind<SiteRepository>() with singleton { SiteRepository(instance()) }
    bind<SiteService>() with singleton { SiteService(instance()) }
}.also {
    logger.info { "DI container created." }
}

class Api(private val di: DI) {
    private val conf: ApiConfig by di.instance()
    private val meterRegistry: MeterRegistry by di.instance()

    @KtorExperimentalAPI
    fun start() {
        val engine = conf.ktor.ktorEngine(ports = listOf(conf.apiPort, conf.metricsPort)) {
            withBaseApiModules()
            withErrorHandling()
            withHealthEndpoint()
            withMetricsModule(meterRegistry, conf.metricsPort)
            apiV1Sites(di)
        }
        engine.start(wait = true)
    }
}

