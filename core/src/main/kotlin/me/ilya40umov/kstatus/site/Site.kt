@file:UseSerializers(LocalDateTimeSerializer::class)

package me.ilya40umov.kstatus.site

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.ilya40umov.kstatus.serialize.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Site(
    val siteId: Int,
    val url: String,
    val createdAt: LocalDateTime,
    val checkIntervalSeconds: Int,
    val lastCheckedAt: LocalDateTime?,
    val lastStatusCheckResult: StatusCheckResult?,
    val nextScheduledFor: LocalDateTime?,
    val lastEnqueuedAt: LocalDateTime?
)