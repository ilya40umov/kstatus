package me.ilya40umov.kstatus.worker

data class SqsMessage<T>(
    val payload: T,
    val receiptHandle: String
)