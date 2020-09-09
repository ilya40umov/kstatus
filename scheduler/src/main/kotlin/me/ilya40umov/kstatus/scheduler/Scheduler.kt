package me.ilya40umov.kstatus.scheduler

import com.github.jasync.sql.db.mysql.MySQLConnection
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder
import com.github.jasync.sql.db.pool.ConnectionPool
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import me.ilya40umov.kstatus.conf.asPoolConfigBuilder
import me.ilya40umov.kstatus.conf.loadConfig
import me.ilya40umov.kstatus.health.withHealthEndpoint
import me.ilya40umov.kstatus.ktor.ktorEngine
import me.ilya40umov.kstatus.ktor.withBaseApiModules
import me.ilya40umov.kstatus.metrics.withMetricsModule
import me.ilya40umov.kstatus.scheduler.sitecheck.SiteCheckQueue
import me.ilya40umov.kstatus.site.SiteRepository
import me.ilya40umov.kstatus.site.SiteService
import me.ilya40umov.kstatus.sqs.buildClient
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.singleton
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@KtorExperimentalAPI
fun main() {
    logger.info { "Starting Scheduler..." }
    val schedulerConfig = loadConfig<SchedulerConfig>()
    logger.info { "Configuration loaded." }
    val di = createDi(schedulerConfig)
    Scheduler(di).start()
}

private fun createDi(schedulerConfig: SchedulerConfig) = DI {
    bind<SchedulerConfig>() with instance(schedulerConfig)
    bind<MeterRegistry>() with singleton { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
    bind<ConnectionPool<MySQLConnection>>() with singleton {
        val config = schedulerConfig.database.asPoolConfigBuilder().build()
        MySQLConnectionBuilder.createConnectionPool(config)
    }
    bind<SiteRepository>() with singleton { SiteRepository(instance()) }
    bind<SiteService>() with singleton { SiteService(instance()) }
    bind<SqsAsyncClient>() with singleton { schedulerConfig.sqs.buildClient() }
    bind<SiteCheckQueue>() with singleton { SiteCheckQueue(schedulerConfig.siteCheck, instance()) }
    bind<SchedulerIteration>() with provider { SchedulerIteration(instance(), instance()) }
}.also {
    logger.info { "DI container created." }
}

@KtorExperimentalAPI
class Scheduler(di: DI) {
    private val conf: SchedulerConfig by di.instance()
    private val meterRegistry: MeterRegistry by di.instance()
    private val newSchedulerIteration: () -> SchedulerIteration by di.provider()

    @KtorExperimentalAPI
    fun start() {
        startKtorInBackground()
        runSchedulerLoop()
    }

    private fun startKtorInBackground() {
        val engine = conf.ktor.ktorEngine(ports = listOf(conf.metricsPort)) {
            withBaseApiModules()
            withHealthEndpoint()
            withMetricsModule(meterRegistry, conf.metricsPort)
        }
        engine.start(wait = false)
    }

    private fun runSchedulerLoop() = runBlocking {
        while (isActive) {
            val schedulerIteration = newSchedulerIteration()
            try {
                schedulerIteration.run()
            } catch (e: Exception) {
                logger.error(e) { "Iteration ${schedulerIteration.uuid} has failed." }
            } finally {
                cooldown(schedulerIteration.startedAt)
            }
        }
    }

    private suspend fun cooldown(iterationStartedAt: LocalDateTime) {
        val iterationTook = ChronoUnit.MILLIS.between(iterationStartedAt, LocalDateTime.now())
        if (iterationTook >= COOLDOWN_MILLIS) {
            return
        }
        delay(COOLDOWN_MILLIS - iterationTook)
    }

    companion object {
        const val COOLDOWN_MILLIS = 3000
    }
}