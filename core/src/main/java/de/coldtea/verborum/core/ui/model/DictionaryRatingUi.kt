package de.coldtea.verborum.core.ui.model

import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.core.ui.UiText
import de.coldtea.verborum.core.utils.ResStrings

/**
 * The user's own rating of a downloaded dictionary, as the rating control shows it. [stars] is
 * null while they have not rated it; [isUpdating] disables the control while a change is in flight.
 */
data class DictionaryRatingUi(
    val stars: Int?,
    val isUpdating: Boolean = false,
)

/** What to tell the user after a rating change; null when it went through. */
fun RatingOutcome.errorMessage(): UiText? = when (this) {
    RatingOutcome.DONE -> null
    RatingOutcome.UNAVAILABLE -> UiText.Resource(ResStrings.errorRatingUnavailable)
    RatingOutcome.NOT_ALLOWED, RatingOutcome.FAILED -> UiText.Resource(ResStrings.errorRatingFailed)
}
