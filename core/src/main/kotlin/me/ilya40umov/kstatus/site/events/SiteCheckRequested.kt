package me.ilya40umov.kstatus.site.events

import kotlinx.serialization.Serializable
import me.ilya40umov.kstatus.serialize.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class SiteCheckRequested(
    val siteId: Int,
    @Serializable(LocalDateTimeSerializer::class)
    val deadline: LocalDateTime
)