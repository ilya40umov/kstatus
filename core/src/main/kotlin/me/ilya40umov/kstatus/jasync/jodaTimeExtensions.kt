package me.ilya40umov.kstatus.jasync

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.TimeZone

fun org.joda.time.LocalDateTime.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(toDate(TimeZone.getDefault()).toInstant(), ZoneOffset.systemDefault())
}

fun LocalDateTime.toJoda(): org.joda.time.LocalDateTime {
    return org.joda.time.LocalDateTime(atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli())
}