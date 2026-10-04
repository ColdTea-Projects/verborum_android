package de.coldtea.verborum.core.profile.ui.profile.model

import de.coldtea.verborum.core.profile.testProfileForm
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileFormTest {

    @Test
    fun `update is disabled while nothing changed`() {
        assertFalse(testProfileForm().canUpdate)
    }

    @Test
    fun `whitespace around an unchanged name is not a change`() {
        assertFalse(testProfileForm(displayName = "  Anna ").canUpdate)
    }

    @Test
    fun `a new name enables update`() {
        assertTrue(testProfileForm(displayName = "Anna Bauer").canUpdate)
    }

    @Test
    fun `ticking the terms with a name enables update`() {
        assertTrue(testProfileForm(agreementAccepted = true).canUpdate)
    }

    @Test
    fun `the terms cannot be accepted with a blank name`() {
        val form = testProfileForm(savedDisplayName = "", agreementAccepted = true)

        assertTrue(form.isDisplayNameMissing)
        assertFalse(form.canUpdate)
    }

    @Test
    fun `a member cannot clear the name while the terms stay ticked`() {
        val form = testProfileForm(savedAgreementAccepted = true, displayName = " ")

        assertFalse(form.canUpdate)
    }

    @Test
    fun `the name may be cleared together with withdrawing`() {
        val form = testProfileForm(savedAgreementAccepted = true, displayName = "", agreementAccepted = false)

        assertTrue(form.canUpdate)
    }

    @Test
    fun `a non-member may clear the name`() {
        assertTrue(testProfileForm(displayName = "").canUpdate)
    }

    @Test
    fun `update is disabled while saving`() {
        assertFalse(testProfileForm(displayName = "Anna Bauer", isSaving = true).canUpdate)
    }
}
