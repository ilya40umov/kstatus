package me.ilya40umov.kstatus.api.routes

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import me.ilya40umov.kstatus.site.Site
import me.ilya40umov.kstatus.site.SiteService
import org.kodein.di.DI
import org.kodein.di.instance

fun Application.apiV1Sites(di: DI) {
    val siteService: SiteService by di.instance()
    routing {
        route("/api/v1/sites") {
            get {
                call.respond(mapOf("data" to siteService.listAll(0, 1)))
            }
            post {
                val receivedSite = call.receive<Site>()
                val createdSite = siteService.create(receivedSite)
                call.respond(mapOf("data" to createdSite))
            }
            get("/{siteId}") {
                val siteId = call.parameters["siteId"]?.toIntOrNull()
                if (siteId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    when (val site = siteService.findById(siteId)) {
                        null -> call.respond(HttpStatusCode.NotFound)
                        site -> call.respond(mapOf("data" to site))
                    }
                }
            }
            put("/{siteId}") {
                val siteId = call.parameters["siteId"]?.toIntOrNull()
                val receivedSite = call.receive<Site>()
                if (siteId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    when (val site = siteService.update(siteId, receivedSite)) {
                        null -> call.respond(HttpStatusCode.NotFound)
                        site -> call.respond(mapOf("data" to site))
                    }
                }
            }
            delete("/{siteId}") {
                val siteId = call.parameters["siteId"]?.toIntOrNull()
                if (siteId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    when (val site = siteService.deleteById(siteId)) {
                        null -> call.respond(HttpStatusCode.NotFound)
                        site -> call.respond(mapOf("data" to site))
                    }
                }
            }
        }
    }
}