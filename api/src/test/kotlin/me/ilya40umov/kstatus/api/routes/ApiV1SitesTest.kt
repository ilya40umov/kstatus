package me.ilya40umov.kstatus.api.routes

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.assertions.json.shouldMatchJson
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.mockk.coEvery
import io.mockk.mockk
import me.ilya40umov.kstatus.api.ApiTestSpec
import me.ilya40umov.kstatus.api.ktor.withErrorHandling
import me.ilya40umov.kstatus.api.oai.validAgainstOpenApi
import me.ilya40umov.kstatus.api.testing.handleJsonRequest
import me.ilya40umov.kstatus.api.testing.withJsonBody
import me.ilya40umov.kstatus.ktor.withBaseApiModules
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

    "GET to /api/v1/sites should return 200 and list of sites" { app, di ->
        val siteService by di.instance<SiteService>()
        coEvery { siteService.listAll(any(), any()) } returns SiteList(
            sites = listOf(site),
            offset = 0,
            totalCount = 1
        )

        app.handleRequest(HttpMethod.Get, "/api/v1/sites").apply {
            this shouldBe validAgainstOpenApi()
            response.status() shouldBe HttpStatusCode.OK
            response.content.shouldContainJsonKeyValue("data.totalCount", 1)
            response.content.shouldContainJsonKeyValue("data.sites[0].url", "http://a.com")
        }
    }

    "GET to /api/v1/sites/{siteId} should return 400 if provided siteId is not valid" { app, _ ->
        app.handleRequest(HttpMethod.Get, "/api/v1/sites/abc").apply {
            this shouldNotBe validAgainstOpenApi()
            response.status() shouldBe HttpStatusCode.BadRequest
            response.content shouldMatchJson """{
                "error": {"status": 400, "message": "Required parameter 'siteId' is missing or invalid."}
            }"""
        }
    }

    "POST to /api/v1/sites/ should return 200 if provided input is valid" { app, di ->
        val siteService by di.instance<SiteService>()
        coEvery { siteService.create(any()) } returns site
        app.handleJsonRequest(HttpMethod.Post, "/api/v1/sites/") {
            withJsonBody(
                """{
                "siteId": 0,
                "url": "http://www.google.com",
                "createdAt": "2020-09-07T11:11:11Z",
                "checkIntervalSeconds": 60       
            }""".trimIndent()
            )
        }.apply {
            this shouldNotBe validAgainstOpenApi()
            response.status() shouldBe HttpStatusCode.OK
            response.content.shouldContainJsonKeyValue("data.url", site.url)
        }
    }

})