package de.coldtea.verborum.bibliotheca.common.data.api

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

class ApiTimestampTest {

    private val originalTimeZone: TimeZone = TimeZone.getDefault()

    @Before
    fun pinTimeZone() {
        // Offset-less values are read in the device's zone, so it has to be fixed for the test.
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    // region parse

    @Test
    fun `parse reads an ISO timestamp with a colon offset`() {
        // 2026-07-19T21:27:20.400+02:00 == 19:27:20.400 UTC
        assertEquals(
            1_784_489_240_400L,
            ApiTimestamp.parse("2026-07-19T21:27:20.400+02:00"),
        )
    }

    @Test
    fun `parse truncates microseconds instead of reading them as millis`() {
        // .400672 must become .400 — reading 400672 as millis would land ~6 minutes late.
        assertEquals(
            1_784_489_240_400L,
            ApiTimestamp.parse("2026-07-19T21:27:20.400672+02:00"),
        )
    }

    @Test
    fun `parse reads a trailing Z as UTC`() {
        assertEquals(
            1_784_489_240_400L,
            ApiTimestamp.parse("2026-07-19T19:27:20.400Z"),
        )
    }

    @Test
    fun `parse reads an offset-less value in the device zone`() {
        // Berlin is UTC+2 in July, so 21:27:20.400 local == 19:27:20.400 UTC.
        assertEquals(
            1_784_489_240_400L,
            ApiTimestamp.parse("2026-07-19T21:27:20.400672"),
        )
    }

    @Test
    fun `parse accepts a value without fractional seconds`() {
        assertEquals(
            1_784_489_240_000L,
            ApiTimestamp.parse("2026-07-19T21:27:20+02:00"),
        )
    }

    @Test
    fun `parse accepts epoch millis sent as a string`() {
        assertEquals(1_784_489_240_400L, ApiTimestamp.parse("1784489240400"))
    }

    @Test
    fun `parse returns null for absent or unusable values`() {
        assertNull(ApiTimestamp.parse(null))
        assertNull(ApiTimestamp.parse(""))
        assertNull(ApiTimestamp.parse("   "))
        assertNull(ApiTimestamp.parse("not a timestamp"))
        assertNull(ApiTimestamp.parse("2026-07-19"))
    }

    // endregion

    // region format

    @Test
    fun `format renders UTC with an explicit offset`() {
        assertEquals(
            "2026-07-19T19:27:20.400+00:00",
            ApiTimestamp.format(1_784_489_240_400L),
        )
    }

    @Test
    fun `format output is readable back by parse`() {
        val epochMillis = 1_784_489_240_400L

        assertEquals(epochMillis, ApiTimestamp.parse(ApiTimestamp.format(epochMillis)))
    }

    // endregion
}
