package me.ilya40umov.kstatus.scheduler.sitecheck

import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ilya40umov.kstatus.site.Site
import me.ilya40umov.kstatus.site.events.SiteCheckRequested
import mu.KotlinLogging
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

class SiteCheckQueue(
    private val config: SiteCheckConfig,
    private val sqsClient: SqsAsyncClient
) {
    /** Returns back a set of IDs that were successfully enqueued. */
    suspend fun enqueueSites(sitesToEnqueue: List<Site>, iterationStartedAt: LocalDateTime): Set<Int> {
        if (sitesToEnqueue.isEmpty()) {
            return emptySet()
        }
        return supervisorScope {
            val sendBatchResponses = sitesToEnqueue.chunked(SQS_BATCH_SIZE).map {
                sqsClient.sendMessageBatch { req ->
                    req.queueUrl(config.sqsQueueUrl)
                    req.entries(
                        sitesToEnqueue.map { site ->
                            SendMessageBatchRequestEntry
                                .builder()
                                .id(site.siteId.toString())
                                .messageBody(
                                    Json.encodeToString(
                                        SiteCheckRequested(
                                            siteId = site.siteId,
                                            deadline = iterationStartedAt.plusSeconds(site.checkIntervalSeconds.toLong())
                                        )
                                    )
                                ).build()
                        }
                    )
                }.asDeferred()
            }
            sendBatchResponses.flatMap { deferred ->
                try {
                    val response = deferred.await()
                    if (response.hasFailed()) {
                        logger.error { "Sending of some messages in the batch failed: ${response.failed()}" }
                    }
                    response
                        .successful()
                        .map { it.id().toInt() }
                } catch (e: Exception) {
                    logger.error(e) { "Call to sendMessageBatch() failed." }
                    listOf()
                }
            }.toSet()
        }
    }

    companion object {
        const val SQS_BATCH_SIZE = 10 // SQS supports up to 10 messages per batch
    }
}