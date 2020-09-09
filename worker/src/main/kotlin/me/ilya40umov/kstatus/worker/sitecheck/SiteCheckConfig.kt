package me.ilya40umov.kstatus.worker.sitecheck

data class SiteCheckConfig(
    val processors: Int,
    val sqsQueueUrl: String
)