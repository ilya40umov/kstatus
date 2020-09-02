@file:UseSerializers(LocalDateTimeSerializer::class)

package me.ilya40umov.kstatus.site

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.ilya40umov.kstatus.serialize.LocalDateTimeSerializer
import java.time.LocalDateTime

// XXX in the real world app we will definitely also need DTOs
@Serializable
data class Site(
    val siteId: Int,
    val url: String,
    val createdAt: LocalDateTime,
    val checkIntervalSeconds: Int,
    val lastCheckedAt: LocalDateTime? = null,
    val lastStatusCheckResult: StatusCheckResult? = null,
    val nextScheduledFor: LocalDateTime? = null,
    val lastEnqueuedAt: LocalDateTime? = null
)