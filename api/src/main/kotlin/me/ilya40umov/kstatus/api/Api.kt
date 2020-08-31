package me.ilya40umov.kstatus.api

import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.ApplicationStopped
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.minimumSize
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import me.ilya40umov.kstatus.api.ktor.KtorEngine
import mu.KotlinLogging
import org.slf4j.event.Level

private val logger = KotlinLogging.logger {}

@KtorExperimentalAPI
fun main(args: Array<String>) = KtorEngine.start(args)

val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

@Suppress("unused")
fun Application.baseModules() {
    install(ContentNegotiation) {
        json()
    }
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024)
        }
    }
    install(MicrometerMetrics) {
        registry = prometheusMeterRegistry
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
    environment.monitor.subscribe(ApplicationStarted) {
        logger.info { "Api started..." }
    }
    environment.monitor.subscribe(ApplicationStopped) {
        logger.info { "Api stopped..." }
    }
}

@Suppress("unused")
fun Application.baseRouting() {
    routing {
        get("/") {
            call.respond(mapOf("hello" to "world"))
        }
        get("/metrics") {
            call.respondText {
                prometheusMeterRegistry.scrape()
            }
        }
    }
}