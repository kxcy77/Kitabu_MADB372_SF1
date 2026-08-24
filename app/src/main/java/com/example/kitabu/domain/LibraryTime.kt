package com.example.kitabu.domain

import com.example.kitabu.domain.model.DueStatus
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

object LibraryTime {
    const val MAX_USER_NAME_LENGTH = 50

    fun normalizedUserName(value: String): String = value.trim().replace(Regex("\\s+"), " ")

    fun isValidUserName(value: String): Boolean {
        val normalized = normalizedUserName(value)
        return normalized.isNotEmpty() && normalized.length <= MAX_USER_NAME_LENGTH
    }

    fun deadlineFrom(clock: Clock, durationDays: Int): Long =
        clock.instant().plus(durationDays.toLong(), ChronoUnit.DAYS).toEpochMilli()

    fun dueStatus(deadline: Long, clock: Clock): DueStatus {
        val zone = clock.zone
        val today = clock.instant().atZone(zone).toLocalDate()
        val deadlineDate = Instant.ofEpochMilli(deadline).atZone(zone).toLocalDate()
        val days = ChronoUnit.DAYS.between(today, deadlineDate)
        return when {
            days > 0 -> DueStatus.Remaining(days)
            days == 0L -> DueStatus.DueToday
            else -> DueStatus.Overdue(-days)
        }
    }

    fun formatDate(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .format(Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate())
}
