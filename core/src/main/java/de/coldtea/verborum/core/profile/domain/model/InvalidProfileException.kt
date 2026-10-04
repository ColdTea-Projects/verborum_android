package de.coldtea.verborum.core.profile.domain.model

/**
 * A profile update broke one of ms_user's rules (400 `InvalidProfileException`, guide §4.4).
 * [detail] is the server's `errorDetail`, written for people — safe to show as-is.
 */
class InvalidProfileException(val detail: String?) : Exception(detail)
