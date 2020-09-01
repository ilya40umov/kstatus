package me.ilya40umov.kstatus.api

import com.sksamuel.hoplite.ConfigLoader
import io.ktor.application.ApplicationStarted
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import me.ilya40umov.kstatus.metrics.metricsModule
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@KtorExperimentalAPI
fun main() {
    logger.info { "Starting Api..." }
    val apiConfig = loadConfig()
    val di = createDi(apiConfig)
    Api(di).start()
}

private fun loadConfig() =
    ConfigLoader().loadConfigOrThrow<ApiConfig>("/application.yaml").also {
        logger.info { "Configuration loaded." }
    }

private fun createDi(apiConfig: ApiConfig) = DI {
    bind<ApiConfig>() with instance(apiConfig)
    bind<MeterRegistry>() with singleton { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
}.also {
    logger.info { "DI container created." }
}

class Api(di: DI) {
    private val conf: ApiConfig by di.instance()
    private val meterRegistry: MeterRegistry by di.instance()

    @KtorExperimentalAPI
    fun start() {
        val env = applicationEngineEnvironment {
            module {
                baseModules()
                metricsModule(meterRegistry, conf.ktor.metricsPort)
                environment.monitor.subscribe(ApplicationStarted) {
                    logger.info { "Api started." }
                }
            }
            connector {
                port = conf.ktor.port
            }
            connector {
                port = conf.ktor.metricsPort
            }
        }
        val engine = embeddedServer(Netty, env)
        engine.addShutdownHook {
            engine.stop(
                conf.ktor.gracePeriodSeconds.toLong(),
                conf.ktor.shutdownTimeoutSeconds.toLong(),
                TimeUnit.SECONDS
            )
        }
        engine.start(wait = true)
    }
}

