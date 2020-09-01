package me.ilya40umov.kstatus.api.site

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import me.ilya40umov.kstatus.site.Site
import me.ilya40umov.kstatus.site.SiteService

@Suppress("UNUSED_PARAMETER")
class SiteController(
    private val siteService: SiteService
) {

    suspend fun listAll(context: PipelineContext<*, ApplicationCall>, subject: Unit) = with(context) {
        call.respond(mapOf("data" to siteService.listAll(0, 1)))
    }

    suspend fun create(context: PipelineContext<*, ApplicationCall>, subject: Unit) = with(context) {
        val receivedSite = call.receive<Site>()
        val createdSite = siteService.create(receivedSite)
        call.respond(mapOf("data" to createdSite))
    }

    suspend fun findById(context: PipelineContext<*, ApplicationCall>, subject: Unit) = with(context) {
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

    suspend fun update(context: PipelineContext<*, ApplicationCall>, subject: Unit) = with(context) {
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

    suspend fun delete(context: PipelineContext<*, ApplicationCall>, subject: Unit) = with(context) {
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