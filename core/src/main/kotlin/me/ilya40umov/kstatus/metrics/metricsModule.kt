package me.ilya40umov.kstatus.metrics

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusMeterRegistry
import me.ilya40umov.kstatus.Messages

fun Application.withMetricsModule(meterRegistry: MeterRegistry, metricsPort: Int) {
    install(MicrometerMetrics) {
        registry = meterRegistry
    }
    (meterRegistry as? PrometheusMeterRegistry)?.also {
        routing {
            get("/metrics") {
                if (call.request.port() == metricsPort) {
                    call.respondText {
                        meterRegistry.scrape()
                    }
                } else {
                    call.respond(status = HttpStatusCode.NotFound, Messages.PAGE_NOT_FOUND_MESSAGE)
                }
            }
        }
    }
}