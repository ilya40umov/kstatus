package me.ilya40umov.kstatus.api.site

import io.ktor.application.Application
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing

fun Application.siteRoutes(siteController: SiteController) {
    routing {
        route("/api/v1") {
            route("/sites") {
                get(siteController::listAll)
                post(siteController::create)
                get("/{siteId}", siteController::findById)
                put("/{siteId}", siteController::update)
                delete("/{siteId}", siteController::delete)
            }
        }
    }
}