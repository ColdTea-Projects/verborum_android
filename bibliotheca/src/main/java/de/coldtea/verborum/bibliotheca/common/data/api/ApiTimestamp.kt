package de.coldtea.verborum.bibliotheca.common.data.api

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Converts between the backend's ISO-8601 timestamps (`creation_dt` / `update_dt`, filled by
 * Hibernate's `@CreationTimestamp` / `@UpdateTimestamp`) and the epoch millis the app stores.
 *
 * Hand-rolled on purpose: `minSdk` is 23 and core library desugaring is not enabled, so
 * `java.time` would throw below API 26. `SimpleDateFormat` is available on every supported level.
 *
 * Tolerated on the wire, because the server may narrow the shape later:
 * - ISO-8601 with offset — `2026-07-19T21:27:20.400672+02:00`, or a trailing `Z`
 * - ISO-8601 without offset — `2026-07-19T17:01:20.906614` (read in the device's zone, see below)
 * - epoch millis as a digits-only string
 *
 * Sub-second precision beyond milliseconds is truncated: the columns carry microseconds
 * (`.906614`) while `SimpleDateFormat`'s `SSS` reads every digit as milliseconds, which would
 * otherwise push a timestamp minutes into the future.
 *
 * A value without an offset is ambiguous — it is read in the device's zone, which is right only
 * while the server and device agree. The backend already emits offsets elsewhere (the response
 * envelope's `timestamp`), so emitting them here too removes the ambiguity entirely.
 */
object ApiTimestamp {

    private const val ISO_NO_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    private const val ISO_WITH_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    private val TRAILING_OFFSET = Regex("""([+-])(\d{2}):?(\d{2})$""")
    private val FRACTIONAL_SECONDS = Regex("""\.(\d+)$""")
    private val NUMERIC_OFFSET_TAIL = Regex("""([+-]\d{2})(\d{2})$""")

    /** Returns epoch millis, or null when [raw] is absent or unparseable. */
    fun parse(raw: String?): Long? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null

        // Already epoch millis.
        value.toLongOrNull()?.let { return it }

        val (normalized, hasOffset) = normalize(value) ?: return null
        val pattern = if (hasOffset) ISO_WITH_OFFSET else ISO_NO_OFFSET

        return runCatching {
            SimpleDateFormat(pattern, Locale.US)
                .apply {
                    isLenient = false
                    // With an offset in the text the parsed zone wins; without one we have to
                    // pick, and the device's zone is the closest available guess.
                    timeZone = TimeZone.getDefault()
                }
                .parse(normalized)
                ?.time
        }.getOrNull()
    }

    /** Renders epoch millis as ISO-8601 in UTC with an explicit `+00:00` offset. */
    fun format(epochMillis: Long): String {
        val rendered = SimpleDateFormat(ISO_WITH_OFFSET, Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(epochMillis))

        // SimpleDateFormat's Z yields "+0000"; ISO-8601 wants "+00:00".
        return NUMERIC_OFFSET_TAIL.replace(rendered) { "${it.groupValues[1]}:${it.groupValues[2]}" }
    }

    /**
     * Rewrites the wire value into something `SimpleDateFormat` can read: offset colon removed
     * (`+02:00` -> `+0200`), `Z` spelled out, and fractional seconds forced to exactly 3 digits.
     */
    private fun normalize(value: String): Pair<String, Boolean>? {
        var dateTime = value
        var offset = ""

        when {
            dateTime.endsWith("Z", ignoreCase = true) -> {
                dateTime = dateTime.dropLast(1)
                offset = "+0000"
            }

            else -> TRAILING_OFFSET.find(dateTime)?.let { match ->
                dateTime = dateTime.removeRange(match.range)
                offset = match.groupValues[1] + match.groupValues[2] + match.groupValues[3]
            }
        }

        if (!dateTime.contains('T')) return null

        val fraction = FRACTIONAL_SECONDS.find(dateTime)
        dateTime = if (fraction == null) {
            "$dateTime.000"
        } else {
            val digits = fraction.groupValues[1].take(3).padEnd(3, '0')
            dateTime.removeRange(fraction.range) + ".$digits"
        }

        return (dateTime + offset) to offset.isNotEmpty()
    }
}
