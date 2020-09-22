package me.ilya40umov.kstatus.api.testing

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest

fun TestApplicationRequest.withJsonBody(json: String) {
    bodyChannel = RequestByteReadChannel(json)
}

fun TestApplicationEngine.handleJsonRequest(
    method: HttpMethod,
    uri: String,
    setup: TestApplicationRequest.() -> Unit = {}
): TestApplicationCall = handleRequest(closeRequest = false) {
    this.uri = uri
    this.method = method
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    setup()
}
