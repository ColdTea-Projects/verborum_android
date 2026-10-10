package de.coldtea.verborum.core.profile.ui.profile

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.profile.domain.ProfileService
import de.coldtea.verborum.core.profile.domain.model.InvalidProfileException
import de.coldtea.verborum.core.profile.testProfileUi
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileForm
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileState
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileUi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileViewModelTest : BaseTest() {

    @MockK
    private lateinit var profileService: ProfileService

    private val profile = MutableStateFlow<ProfileUi?>(null)

    override fun setUp() {
        super.setUp()
        every { profileService.observeProfile() } returns profile
        coEvery { profileService.refreshProfile() } coAnswers { testProfileUi().also { profile.value = it } }
    }

    private fun successForm(viewModel: ProfileViewModel): ProfileForm =
        (viewModel.state.value as ProfileState.Success).form

    // region load
    @Test
    fun `init loads the profile into the form`() = runTest {
        val viewModel = ProfileViewModel(profileService)

        assertEquals(ProfileState.Success(ProfileForm.from(testProfileUi())), viewModel.state.value)
    }

    @Test
    fun `a failed first load is Failed, and retry loads again`() = runTest {
        coEvery { profileService.refreshProfile() } throws RuntimeException("offline")
        val viewModel = ProfileViewModel(profileService)
        assertEquals(ProfileState.Failed, viewModel.state.value)

        coEvery { profileService.refreshProfile() } coAnswers { testProfileUi().also { profile.value = it } }
        viewModel.refresh()

        assertEquals(ProfileState.Success(ProfileForm.from(testProfileUi())), viewModel.state.value)
    }
    // endregion

    // region editing
    @Test
    fun `the display name is capped at the backend limit`() = runTest {
        val viewModel = ProfileViewModel(profileService)

        viewModel.onDisplayNameChange("a".repeat(300))

        assertEquals(255, successForm(viewModel).displayName.length)
    }

    @Test
    fun `update sends only the changed fields`() = runTest {
        coEvery { profileService.updateProfile(any(), any()) } returns testProfileUi()
        val viewModel = ProfileViewModel(profileService)

        viewModel.onAgreementChange(true)
        viewModel.update()

        coVerify { profileService.updateProfile(displayName = null, marketplaceAgreementAccepted = true) }
    }

    @Test
    fun `update sends the trimmed new name`() = runTest {
        coEvery { profileService.updateProfile(any(), any()) } returns testProfileUi()
        val viewModel = ProfileViewModel(profileService)

        viewModel.onDisplayNameChange("  Anna Bauer ")
        viewModel.update()

        coVerify { profileService.updateProfile(displayName = "Anna Bauer", marketplaceAgreementAccepted = null) }
    }

    @Test
    fun `update does nothing while it is not allowed`() = runTest {
        val viewModel = ProfileViewModel(profileService)

        viewModel.onDisplayNameChange("")
        viewModel.onAgreementChange(true)
        viewModel.update()

        coVerify(exactly = 0) { profileService.updateProfile(any(), any()) }
    }

    @Test
    fun `a successful update resets the form to the reloaded profile`() = runTest {
        val joined = testProfileUi(isMarketplaceMember = true)
        coEvery { profileService.updateProfile(any(), any()) } coAnswers { joined.also { profile.value = it } }
        val viewModel = ProfileViewModel(profileService)

        viewModel.onAgreementChange(true)
        viewModel.update()

        assertEquals(ProfileForm.from(joined), successForm(viewModel))
    }

    @Test
    fun `a rejected update keeps the drafts and stops saving`() = runTest {
        coEvery { profileService.updateProfile(any(), any()) } throws InvalidProfileException("nope")
        val viewModel = ProfileViewModel(profileService)

        viewModel.onDisplayNameChange("Anna Bauer")
        viewModel.update()

        val form = successForm(viewModel)
        assertEquals("Anna Bauer", form.displayName)
        assertFalse(form.isSaving)
    }
    // endregion

    // region leaving the Forum
    private fun memberViewModel(): ProfileViewModel {
        coEvery { profileService.refreshProfile() } coAnswers {
            testProfileUi(isMarketplaceMember = true).also { profile.value = it }
        }
        return ProfileViewModel(profileService)
    }

    @Test
    fun `unticking the terms asks for confirmation instead of saving`() = runTest {
        val viewModel = memberViewModel()

        viewModel.onAgreementChange(false)
        viewModel.update()

        assertTrue(successForm(viewModel).isLeaveConfirmationShown)
        coVerify(exactly = 0) { profileService.updateProfile(any(), any()) }
    }

    @Test
    fun `confirming leaves the Forum`() = runTest {
        coEvery { profileService.updateProfile(any(), any()) } returns testProfileUi()
        val viewModel = memberViewModel()

        viewModel.onAgreementChange(false)
        viewModel.update()
        viewModel.confirmLeaveForum()

        assertFalse(successForm(viewModel).isLeaveConfirmationShown)
        coVerify { profileService.updateProfile(displayName = null, marketplaceAgreementAccepted = false) }
    }

    @Test
    fun `dismissing keeps the drafts and sends nothing`() = runTest {
        val viewModel = memberViewModel()

        viewModel.onAgreementChange(false)
        viewModel.update()
        viewModel.dismissLeaveConfirmation()

        val form = successForm(viewModel)
        assertFalse(form.isLeaveConfirmationShown)
        assertFalse(form.agreementAccepted)
        coVerify(exactly = 0) { profileService.updateProfile(any(), any()) }
    }

    @Test
    fun `renaming a member needs no confirmation`() = runTest {
        coEvery { profileService.updateProfile(any(), any()) } returns testProfileUi()
        val viewModel = memberViewModel()

        viewModel.onDisplayNameChange("Anna Bauer")
        viewModel.update()

        assertFalse(successForm(viewModel).isLeaveConfirmationShown)
        coVerify { profileService.updateProfile(displayName = "Anna Bauer", marketplaceAgreementAccepted = null) }
    }
    // endregion
}
