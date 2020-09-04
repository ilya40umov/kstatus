package me.ilya40umov.kstatus.conf

data class KtorConfig(
    val gracePeriodSeconds: Int = 1,
    val shutdownTimeoutSeconds: Int = 1
)