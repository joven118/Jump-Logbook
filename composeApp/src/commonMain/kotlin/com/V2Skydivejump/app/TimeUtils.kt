package com.V2Skydivejump.app

import kotlinx.datetime.*

expect fun nowMillis(): Long

object TimeUtils {
    fun nowEpochMillis(): Long = nowMillis()
    
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun formatEpochMillis(millis: Long): String {
        val instant = Instant.fromEpochMilliseconds(millis)
        val period = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthStr = months.getOrNull(period.monthNumber - 1) ?: ""
        return "${period.dayOfMonth} $monthStr ${period.year}"
    }
}
