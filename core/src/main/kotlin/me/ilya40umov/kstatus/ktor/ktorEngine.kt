package me.ilya40umov.kstatus.ktor

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import me.ilya40umov.kstatus.conf.KtorConfig
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@KtorExperimentalAPI
fun KtorConfig.ktorEngine(ports: List<Int>, setupModules: Application.() -> Unit): ApplicationEngine {
    require(ports.isNotEmpty()) { "At least one port should be provided for Ktor to listen on." }
    val env = applicationEngineEnvironment {
        module {
            setupModules()
            environment.monitor.subscribe(ApplicationStarted) {
                logger.info { "Ktor started." }
            }
        }
        ports.forEach {
            connector { port = it }
        }
    }
    val engine = embeddedServer(Netty, env)
    engine.addShutdownHook {
        engine.stop(
            gracePeriodSeconds.toLong(),
            shutdownTimeoutSeconds.toLong(),
            TimeUnit.SECONDS
        )
    }
    return engine
}