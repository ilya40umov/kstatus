package me.ilya40umov.kstatus.worker.sitecheck

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.future.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.ilya40umov.kstatus.site.events.SiteCheckRequested
import me.ilya40umov.kstatus.worker.SqsMessage
import mu.KotlinLogging
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest

private val logger = KotlinLogging.logger {}

class SiteCheckConsumer(
        private val config: SiteCheckConfig,
        private val sqsClient: SqsAsyncClient,
        private val handler: SiteCheckHandler
) {
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisorJob)

    fun start(): Job = scope.launch {
        val messageChannel = Channel<SqsMessage<SiteCheckRequested>>()
        repeat(config.processors) { launchProcessor(messageChannel) }
        launchSqsPoller(messageChannel)
    }

    fun stop() {
        supervisorJob.cancel()
    }

    private fun CoroutineScope.launchSqsPoller(channel: SendChannel<SqsMessage<SiteCheckRequested>>) = launch {
        repeatUntilCancelled {
            val receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(config.sqsQueueUrl)
                    .waitTimeSeconds(SQS_WAIT_TIME_SECONDS)
                    .maxNumberOfMessages(SQS_MAX_MESSAGES)
                    .build()

            val response = sqsClient.receiveMessage(receiveRequest).await()
            if (response.hasMessages()) {
                response.messages().forEach { message ->
                    channel.send(
                        SqsMessage(
                            payload = Json.decodeFromString(message.body()),
                            receiptHandle = message.receiptHandle()
                        )
                    )
                }
            }
        }
    }

    private fun CoroutineScope.launchProcessor(channel: ReceiveChannel<SqsMessage<SiteCheckRequested>>) = launch {
        repeatUntilCancelled {
            for (message in channel) {
                try {
                    handler.onSiteCheckRequested(message.payload)
                } catch (e: Exception) {
                    logger.error { "Failed to process: $message" }
                } finally {
                    withContext(NonCancellable) {
                        sqsClient.deleteMessage { req ->
                            req.queueUrl(config.sqsQueueUrl)
                            req.receiptHandle(message.receiptHandle)
                        }.await()
                    }
                }
            }
        }
    }

    private suspend fun CoroutineScope.repeatUntilCancelled(block: suspend () -> Unit) {
        while (isActive) {
            try {
                block()
                yield()
            } catch (e: CancellationException) {
                logger.info { "Coroutine cancelled." }
            } catch (e: Exception) {
                logger.error(e) { "Provided block of code failed." }
            }
        }
    }

    companion object {
        const val SQS_WAIT_TIME_SECONDS = 20
        const val SQS_MAX_MESSAGES = 10
    }
}