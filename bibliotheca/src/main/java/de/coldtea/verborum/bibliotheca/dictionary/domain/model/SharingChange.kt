package de.coldtea.verborum.bibliotheca.dictionary.domain.model

/** How a share/unshare request ended. */
enum class SharingChange {
    /** The server applied it; the local row follows. */
    UPDATED,

    /** Refused: it is the member's last shared dictionary (400 `SharingRequiredException`). */
    LAST_SHARED,

    /** Offline, or any other failure. Nothing changed. */
    FAILED,
}
