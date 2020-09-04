package me.ilya40umov.kstatus.site.events

import java.time.LocalDateTime

data class SiteProcessedByScheduler(
    val siteId: Int,
    val scheduledFor: LocalDateTime,
    val enqueuedAt: LocalDateTime? = null
)