package me.ilya40umov.kstatus.scheduler

import com.github.jasync.sql.db.mysql.MySQLConnection
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder
import com.github.jasync.sql.db.pool.ConnectionPool
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ilya40umov.kstatus.conf.asPoolConfigBuilder
import me.ilya40umov.kstatus.conf.loadConfig
import me.ilya40umov.kstatus.health.withHealthEndpoint
import me.ilya40umov.kstatus.ktor.ktorEngine
import me.ilya40umov.kstatus.ktor.withBaseApiModules
import me.ilya40umov.kstatus.metrics.withMetricsModule
import me.ilya40umov.kstatus.site.Site
import me.ilya40umov.kstatus.site.SiteRepository
import me.ilya40umov.kstatus.site.SiteService
import me.ilya40umov.kstatus.site.events.SiteProcessedByScheduler
import me.ilya40umov.kstatus.sqs.buildClient
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry
import java.lang.Long.max
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
}.also {
    logger.info { "DI container created." }
}

@KtorExperimentalAPI
class Scheduler(di: DI) {
    private val conf: SchedulerConfig by di.instance()
    private val meterRegistry: MeterRegistry by di.instance()
    private val siteService: SiteService by di.instance()
    private val sqsAsyncClient: SqsAsyncClient by di.instance()

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
        while (true) {
            val iterationStartedAt = LocalDateTime.now()
            try {
                doIteration(iterationStartedAt)
            } catch (e: Exception) {
                logger.error(e) { "Iteration has failed." }
            } finally {
                val coolDownInMillis = max(
                    0, COOLDOWN_MILLIS - ChronoUnit.MILLIS.between(iterationStartedAt, LocalDateTime.now())
                )
                delay(coolDownInMillis)
            }
        }
    }

    private suspend fun doIteration(iterationStartedAt: LocalDateTime) {
        val sites = siteService.listWhereNextScheduledForIsBefore(
            before = iterationStartedAt,
            limit = DB_BATCH_SIZE
        ).also {
            logger.info { "Retrieved ${it.size} sites to process..." }
        }

        val sitesToEnqueue = sites.filter { site ->
            val lastEnqueuedAt = site.lastEnqueuedAt
            if (lastEnqueuedAt == null) {
                true
            } else {
                val whenToEnqueue = lastEnqueuedAt.plusSeconds(site.checkIntervalSeconds.toLong())
                whenToEnqueue <= iterationStartedAt
            }
        }

        val enqueuedSiteIds = enqueueSitesForStatusCheck(sitesToEnqueue)
        logger.info { "Enqueued ${enqueuedSiteIds.size} sites." }

        val enqueuingFailedForSiteIds = sitesToEnqueue.asSequence().map { it.siteId }.toSet() - enqueuedSiteIds
        val processedSites = sites.filter { it.siteId !in enqueuingFailedForSiteIds }.map { site ->
            SiteProcessedByScheduler(
                siteId = site.siteId,
                scheduledFor = if (enqueuedSiteIds.contains(site.siteId) || site.lastEnqueuedAt == null) {
                    iterationStartedAt.plusSeconds(site.checkIntervalSeconds.toLong())
                } else {
                    site.lastEnqueuedAt!!.plusSeconds(site.checkIntervalSeconds.toLong())
                },
                enqueuedAt = if (enqueuedSiteIds.contains(site.siteId)) iterationStartedAt else site.lastEnqueuedAt
            )
        }
        if (processedSites.isNotEmpty()) {
            siteService.onSitesProcessedByScheduler(processedSites)
        }
    }

    private suspend fun enqueueSitesForStatusCheck(sitesToEnqueue: List<Site>): Set<Int> {
        if (sitesToEnqueue.isEmpty()) {
            return emptySet()
        }
        return supervisorScope {
            val sendBatchResponses = sitesToEnqueue.chunked(SQS_BATCH_SIZE).map {
                sqsAsyncClient.sendMessageBatch(
                    SendMessageBatchRequest.builder()
                        .queueUrl(conf.sqs.workerQueueUrl)
                        .entries(sitesToEnqueue.map { site ->
                            SendMessageBatchRequestEntry
                                .builder()
                                .id(site.siteId.toString())
                                .messageBody(
                                    Json.encodeToString(mapOf("siteId" to site.siteId))
                                ).build()
                        })
                        .build()
                ).asDeferred()
            }
            sendBatchResponses.flatMap { deferred ->
                try {
                    val response = deferred.await()
                    if (response.hasFailed()) {
                        logger.error { "Sending of some messages in the batch failed: ${response.failed()}" }
                    }
                    response
                        .successful()
                        .map { it.id().toInt() }
                } catch (e: Exception) {
                    logger.error(e) { "Call to sendMessageBatch() failed." }
                    listOf()
                }
            }.toSet()
        }
    }

    companion object {
        const val DB_BATCH_SIZE = 50
        const val SQS_BATCH_SIZE = 10 // SQS supports up to 10 messages per batch
        const val COOLDOWN_MILLIS = 3000
    }

}