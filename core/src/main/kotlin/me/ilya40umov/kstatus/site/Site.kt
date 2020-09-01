package me.ilya40umov.kstatus.site

data class Site(
    val id: Int?,
    val url: String
    // how often to ping
    // createdAt
    // lastEnqueuedAt
    // lastCheckedAt
    // last check result (UP or DOWN)
)