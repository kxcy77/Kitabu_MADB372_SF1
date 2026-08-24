package com.example.kitabu.domain

import com.example.kitabu.domain.model.DueStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class LibraryTimeTest {
    private val zone = ZoneId.of("Africa/Johannesburg")
    private val instant = Instant.parse("2026-08-24T10:00:00Z")
    private val clock = Clock.fixed(instant, zone)

    @Test
    fun `normalizes and validates user names`() {
        assertEquals("Demo Student", LibraryTime.normalizedUserName("  Demo   Student "))
        assertTrue(LibraryTime.isValidUserName("Demo Student"))
        assertFalse(LibraryTime.isValidUserName("   "))
        assertFalse(LibraryTime.isValidUserName("x".repeat(51)))
    }

    @Test
    fun `deadline uses selected duration`() {
        val expected = instant.plus(14, ChronoUnit.DAYS).toEpochMilli()
        assertEquals(expected, LibraryTime.deadlineFrom(clock, 14))
    }

    @Test
    fun `due status handles future today and overdue dates`() {
        assertEquals(DueStatus.Remaining(3), LibraryTime.dueStatus(instant.plus(3, ChronoUnit.DAYS).toEpochMilli(), clock))
        assertEquals(DueStatus.DueToday, LibraryTime.dueStatus(instant.plus(2, ChronoUnit.HOURS).toEpochMilli(), clock))
        assertEquals(DueStatus.Overdue(2), LibraryTime.dueStatus(instant.minus(2, ChronoUnit.DAYS).toEpochMilli(), clock))
    }
}
