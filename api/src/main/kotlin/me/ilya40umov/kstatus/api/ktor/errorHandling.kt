package me.ilya40umov.kstatus.api.ktor

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import me.ilya40umov.kstatus.Messages
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun Application.withErrorHandling() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) {
            call.respondWithError(HttpStatusCode.NotFound, Messages.NOT_FOUND_MESSAGE)
        }
        exception<io.ktor.features.ContentTransformationException> { e ->
            call.respondWithError(HttpStatusCode.BadRequest, e.message ?: Messages.BAD_REQUEST_MESSAGE)
        }
        exception<kotlinx.serialization.SerializationException> { e ->
            call.respondWithError(HttpStatusCode.BadRequest, e.message ?: Messages.BAD_REQUEST_MESSAGE)
        }
        exception<java.time.format.DateTimeParseException> { e ->
            call.respondWithError(HttpStatusCode.BadRequest, e.message ?: Messages.BAD_REQUEST_MESSAGE)
        }
        exception<Throwable> { throwable ->
            logger.error(throwable) { "Caught an unexpected exception." }
            call.respondWithError(HttpStatusCode.InternalServerError, Messages.INTERNAL_ERROR_MESSAGE)
        }
    }
}