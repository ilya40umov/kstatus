package me.ilya40umov.kstatus.worker.sitecheck

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import me.ilya40umov.kstatus.site.SiteService
import me.ilya40umov.kstatus.site.StatusCheckResult
import me.ilya40umov.kstatus.site.events.SiteCheckRequested
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SiteCheckHandler(
    private val siteService: SiteService,
    private val httpClient: HttpClient
) {
    suspend fun onSiteCheckRequested(siteCheckRequested: SiteCheckRequested) {
        val site = siteService.findById(siteCheckRequested.siteId) ?: return
        // XXX we should probably wrap it into a try-catch and consider site DOWN when we get any sort of a timeout
        val response = httpClient.get<HttpResponse>(site.url)
        val checkResult = when (response.status) {
            HttpStatusCode.OK -> StatusCheckResult.UP
            else -> StatusCheckResult.DOWN
        }
        logger.info { "Check result: $checkResult - ${site.url}" }
        siteService.updateSiteCheckResult(site.siteId, checkResult)
    }
}