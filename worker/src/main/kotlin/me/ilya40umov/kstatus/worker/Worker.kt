package me.ilya40umov.kstatus.worker

import com.github.jasync.sql.db.mysql.MySQLConnection
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder
import com.github.jasync.sql.db.pool.ConnectionPool
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import me.ilya40umov.kstatus.conf.asPoolConfigBuilder
import me.ilya40umov.kstatus.conf.loadConfig
import me.ilya40umov.kstatus.health.withHealthEndpoint
import me.ilya40umov.kstatus.ktor.ktorEngine
import me.ilya40umov.kstatus.ktor.withBaseApiModules
import me.ilya40umov.kstatus.metrics.withMetricsModule
import me.ilya40umov.kstatus.site.SiteRepository
import me.ilya40umov.kstatus.site.SiteService
import me.ilya40umov.kstatus.sqs.buildClient
import me.ilya40umov.kstatus.worker.sitecheck.SiteCheckConsumer
import me.ilya40umov.kstatus.worker.sitecheck.SiteCheckHandler
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import software.amazon.awssdk.services.sqs.SqsAsyncClient

private val logger = KotlinLogging.logger {}

@KtorExperimentalAPI
fun main() {
    logger.info { "Starting Worker..." }
    val workerConfig = loadConfig<WorkerConfig>()
    logger.info { "Configuration loaded." }
    val di = createDi(workerConfig)
    Worker(di).start()
}

private fun createDi(workerConfig: WorkerConfig) = DI {
    bind<WorkerConfig>() with instance(workerConfig)
    bind<MeterRegistry>() with singleton { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
    bind<ConnectionPool<MySQLConnection>>() with singleton {
        val config = workerConfig.database.asPoolConfigBuilder().build()
        MySQLConnectionBuilder.createConnectionPool(config)
    }
    bind<HttpClient>() with singleton { HttpClient(Apache) }
    bind<SiteRepository>() with singleton { SiteRepository(instance()) }
    bind<SiteService>() with singleton { SiteService(instance()) }
    bind<SqsAsyncClient>() with singleton { workerConfig.sqs.buildClient() }
    bind<SiteCheckHandler>() with singleton { SiteCheckHandler(instance(), instance()) }
    bind<SiteCheckConsumer>() with singleton { SiteCheckConsumer(workerConfig.siteCheck, instance(), instance()) }
}.also {
    logger.info { "DI container created." }
}

@KtorExperimentalAPI
class Worker(di: DI) {
    private val conf: WorkerConfig by di.instance()
    private val meterRegistry: MeterRegistry by di.instance()
    private val siteCheckConsumer: SiteCheckConsumer by di.instance()

    @KtorExperimentalAPI
    fun start() {
        startKtorInBackground()
        startConsumersAndWait()
    }

    private fun startKtorInBackground() {
        val engine = conf.ktor.ktorEngine(ports = listOf(conf.metricsPort)) {
            withBaseApiModules()
            withHealthEndpoint()
            withMetricsModule(meterRegistry, conf.metricsPort)
        }
        engine.start(wait = false)
    }

    private fun startConsumersAndWait() = runBlocking {
        val siteCheckJob = siteCheckConsumer.start()
        Runtime.getRuntime().addShutdownHook(
            object : Thread() {
                override fun run() {
                    siteCheckConsumer.stop()
                }
            }
        )
        joinAll(siteCheckJob)
    }
}