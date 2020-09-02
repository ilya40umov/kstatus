package me.ilya40umov.kstatus.api.ktor

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import me.ilya40umov.kstatus.api.ApiError

suspend inline fun <T> ApplicationCall.respondWithPayload(data: T) {
    respond(mapOf("data" to data))
}

suspend inline fun ApplicationCall.respondWithError(status: HttpStatusCode, message: String) {
    respond(status, mapOf("error" to ApiError(status.value, message)))
}