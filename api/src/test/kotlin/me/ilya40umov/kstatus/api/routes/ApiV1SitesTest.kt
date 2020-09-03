package me.ilya40umov.kstatus.api.routes

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.assertions.json.shouldMatchJson
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.mockk.coEvery
import io.mockk.mockk
import me.ilya40umov.kstatus.api.ApiTestSpec
import me.ilya40umov.kstatus.api.ktor.withBaseApiModules
import me.ilya40umov.kstatus.api.ktor.withErrorHandling
import me.ilya40umov.kstatus.site.Site
import me.ilya40umov.kstatus.site.SiteList
import me.ilya40umov.kstatus.site.SiteService
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.time.LocalDateTime

class ApiV1SitesTest : ApiTestSpec({
    bind<SiteService>() with singleton { mockk() }
}, { di ->
    withBaseApiModules()
    withErrorHandling()
    apiV1Sites(di)
}, {

    val site = Site(
        siteId = 1,
        url = "http://a.com",
        createdAt = LocalDateTime.now(),
        checkIntervalSeconds = 60
    )

    "/api/v1/sites should return 200 and list of sites" { app, di ->
        val siteService by di.instance<SiteService>()
        coEvery { siteService.listAll(any(), any()) } returns SiteList(
            sites = listOf(site),
            offset = 0,
            totalCount = 1
        )

        app.handleRequest(HttpMethod.Get, "/api/v1/sites").apply {
            response.status() shouldBe HttpStatusCode.OK
            response.content.shouldContainJsonKeyValue("data.totalCount", 1)
            response.content.shouldContainJsonKeyValue("data.sites[0].url", "http://a.com")
        }
    }

    "/api/v1/sites/{siteId} should return 400 if provided siteId is not valid" { app, _ ->
        app.handleRequest(HttpMethod.Get, "/api/v1/sites/abc").apply {
            response.status() shouldBe HttpStatusCode.BadRequest
            response.content shouldMatchJson """{
                "error": {"status": 400, "message": "Required parameter 'siteId' is missing or invalid."}
            }"""
        }
    }

})