package de.coldtea.verborum.core.ui.model

import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.core.ui.UiText
import de.coldtea.verborum.core.utils.ResStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DictionaryRatingUiTest {

    @Test
    fun `only a failed change has a message`() {
        assertNull(RatingOutcome.DONE.errorMessage())
        assertEquals(UiText.Resource(ResStrings.errorRatingUnavailable), RatingOutcome.UNAVAILABLE.errorMessage())
        assertEquals(UiText.Resource(ResStrings.errorRatingFailed), RatingOutcome.NOT_ALLOWED.errorMessage())
        assertEquals(UiText.Resource(ResStrings.errorRatingFailed), RatingOutcome.FAILED.errorMessage())
    }
}
