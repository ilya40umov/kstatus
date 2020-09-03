package me.ilya40umov.kstatus.api.routes

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import me.ilya40umov.kstatus.api.ktor.respondWithError
import me.ilya40umov.kstatus.api.ktor.respondWithPayload
import me.ilya40umov.kstatus.site.Site
import me.ilya40umov.kstatus.site.SiteService
import org.kodein.di.DI
import org.kodein.di.instance

fun Application.apiV1Sites(di: DI) {
    val siteService: SiteService by di.instance()
    routing {
        route("/api/v1/sites") {
            get {
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 10
                val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
                call.respondWithPayload(data = siteService.listAll(offset, limit))
            }
            post {
                val receivedSite = call.receive<Site>()
                val createdSite = siteService.create(receivedSite)
                call.respondWithPayload(data = createdSite)
            }
            get("/{siteId}") {
                val siteId = call.parameters["siteId"]?.toIntOrNull()
                if (siteId == null) {
                    call.respondWithError(
                        HttpStatusCode.BadRequest,
                        "Required parameter 'siteId' is missing or invalid."
                    )
                } else {
                    when (val site = siteService.findById(siteId)) {
                        null -> call.respondWithError(HttpStatusCode.NotFound, "Site not found.")
                        site -> call.respondWithPayload(data = site)
                    }
                }
            }
            put("/{siteId}") {
                val siteId = call.parameters["siteId"]?.toIntOrNull()
                val receivedSite = call.receive<Site>()
                if (siteId == null) {
                    call.respondWithError(HttpStatusCode.BadRequest, "Required parameter 'siteId' is missing.")
                } else {
                    when (val site = siteService.update(siteId, receivedSite)) {
                        null -> call.respondWithError(HttpStatusCode.NotFound, "Site not found.")
                        site -> call.respondWithPayload(data = site)
                    }
                }
            }
            delete("/{siteId}") {
                val siteId = call.parameters["siteId"]?.toIntOrNull()
                if (siteId == null) {
                    call.respondWithError(HttpStatusCode.BadRequest, "Required parameter 'siteId' is missing.")
                } else {
                    when (val site = siteService.deleteById(siteId)) {
                        null -> call.respondWithError(HttpStatusCode.NotFound, "Site not found.")
                        site -> call.respondWithPayload(data = site)
                    }
                }
            }
        }
    }
}