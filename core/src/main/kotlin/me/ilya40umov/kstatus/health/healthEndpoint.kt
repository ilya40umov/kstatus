package me.ilya40umov.kstatus.health

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing

fun Application.withHealthEndpoint() {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "UP"))
        }
    }
}