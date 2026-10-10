package de.coldtea.verborum.core.library.domain.model

/** How a request to rate (or un-rate) a dictionary ended (`docs/marketplace-client-guide.md` §5.6). */
enum class RatingOutcome {
    /** The marketplace recorded it. */
    DONE,

    /** The dictionary is no longer listed: made private, deleted, or its publisher left the Forum (404). */
    UNAVAILABLE,

    /** Refused: not a member, not downloaded, or the user's own dictionary (403 / 400). */
    NOT_ALLOWED,

    /** Offline, or any other failure. Nothing changed. */
    FAILED,
}
