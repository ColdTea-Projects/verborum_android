package de.coldtea.verborum.bibliotheca.dictionary.domain.model

/** How a request to delete one of the user's dictionaries ended. */
enum class DictionaryDeletion {
    /** The server deleted it (and its words); the local rows are gone too. */
    DELETED,

    /** The server could not be reached or failed: the tombstone stays and sync retries. */
    PENDING,

    /**
     * Refused: it is the member's last shared dictionary while private ones remain (400
     * `SharingRequiredException`, marketplace guide §4.6). Nothing changed; it stays.
     */
    SHARING_REQUIRED,

    /** Refused for another reason (any other 400). Nothing changed; it stays. */
    REFUSED,
}
