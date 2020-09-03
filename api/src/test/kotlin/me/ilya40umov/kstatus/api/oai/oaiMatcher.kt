package me.ilya40umov.kstatus.api.oai

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.Response
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.report.JsonValidationReportFormat
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.request.contentType
import io.ktor.request.path
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.util.toByteArray
import kotlinx.coroutines.runBlocking

fun TestApplicationCall.shouldBeValidAgainstOpenApi() = this shouldBe validAgainstOpenApi()
fun TestApplicationCall.shouldNotBeValidAgainstOpenApi() = this shouldNotBe validAgainstOpenApi()

fun validAgainstOpenApi(): Matcher<TestApplicationCall> = object : Matcher<TestApplicationCall> {
    override fun test(value: TestApplicationCall): MatcherResult {
        val request = value.request.toValidatorRequest()
        val response = value.response.toValidatorResponse()
        val validationReport = apiValidator.validate(request, response)
        val reportAsString = JsonValidationReportFormat.getInstance().apply(validationReport)
        return MatcherResult(
            !validationReport.hasErrors(),
            "Provided request-response pair is not valid against the spec. $reportAsString",
            "Provided request-response should not be valid against the spec, but it is."
        )
    }
}

private val apiValidator: OpenApiInteractionValidator by lazy {
    val rootDir = System.getenv("ROOT_DIR") ?: "${System.getenv("PWD")}/.."
    OpenApiInteractionValidator
        .createFor("$rootDir/openapi.yaml")
        .build()
}

private fun TestApplicationRequest.toValidatorRequest(): Request {
    val builder = SimpleRequest.Builder(method.value, path())
    builder.withContentType(contentType().run { "$contentType/$contentSubtype" })
    runBlocking { builder.withBody(bodyChannel.toByteArray().decodeToString()) }
    headers.entries().forEach { (name, value) ->
        builder.withHeader(name, value)
    }
    queryParameters.entries().forEach { (name, values) ->
        builder.withQueryParam(name, values)
    }
    return builder.build()
}

private fun TestApplicationResponse.toValidatorResponse(): Response {
    val builder = SimpleResponse.Builder(status()!!.value)
    content?.also { builder.withBody(it) }
    headers.allValues().entries().forEach { (name, value) ->
        builder.withHeader(name, value)
    }
    return builder.build()
}