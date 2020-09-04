package me.ilya40umov.kstatus.sqs

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.net.URI

data class SqsConfig(
    val region: String,
    val endpointOverride: String?,
    val workerQueueUrl: String,
    val credentials: AwsCredentials
)

data class AwsCredentials(
    val accessKeyId: String,
    val secretAccessKey: String
)

fun SqsConfig.buildClient(): SqsAsyncClient {
    val sqsClientBuilder = SqsAsyncClient.builder()
        .region(Region.of(region))
        .credentialsProvider {
            AwsBasicCredentials.create(
                credentials.accessKeyId,
                credentials.secretAccessKey
            )
        }
    endpointOverride?.also { sqsClientBuilder.endpointOverride(URI.create(it)) }
    return sqsClientBuilder.build()
}