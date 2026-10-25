package de.coldtea.verborum.core.profile.ui.deleteaccount

import android.content.Intent
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.auth.domain.AuthService
import de.coldtea.verborum.core.auth.domain.model.ReauthenticationOutcome
import de.coldtea.verborum.core.profile.domain.AccountDeletionService
import de.coldtea.verborum.core.profile.domain.model.ReauthenticationRequiredException
import de.coldtea.verborum.core.profile.ui.deleteaccount.model.DeleteAccountState
import de.coldtea.verborum.core.profile.ui.deleteaccount.model.DeleteAccountStep
import de.coldtea.verborum.core.ui.UiText
import de.coldtea.verborum.core.utils.ResStrings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeleteAccountViewModelTest : BaseTest() {

    @MockK
    private lateinit var authService: AuthService

    @MockK
    private lateinit var accountDeletionService: AccountDeletionService

    private val browserIntent = mockk<Intent>()
    private val redirectData = mockk<Intent>()

    private lateinit var viewModel: DeleteAccountViewModel

    override fun setUp() {
        super.setUp()
        every { authService.reauthenticationIntent() } returns browserIntent
        coEvery { authService.completeReauthentication(redirectData) } returns ReauthenticationOutcome.Success
        coEvery { accountDeletionService.deleteAccount() } returns Unit
        viewModel = DeleteAccountViewModel(authService, accountDeletionService)
    }

    private fun step() = viewModel.state.value.step

    private fun acknowledgedWarning() {
        viewModel.onDeleteClick()
        viewModel.onAcknowledgedChange(true)
    }

    private fun signedInAgain() {
        acknowledgedWarning()
        viewModel.reauthenticationIntent()
        viewModel.onReauthenticationResult(redirectData)
    }

    // region warning
    @Test
    fun `tapping Delete opens the warning with the checkbox off`() {
        viewModel.onDeleteClick()

        assertEquals(DeleteAccountState(DeleteAccountStep.Warning, acknowledged = false), viewModel.state.value)
    }

    @Test
    fun `Continue gives no sign-in page until the checkbox is on`() {
        viewModel.onDeleteClick()

        assertNull(viewModel.reauthenticationIntent())
        assertEquals(DeleteAccountStep.Warning, step())
        verify(exactly = 0) { authService.reauthenticationIntent() }
    }

    @Test
    fun `Cancel on the warning returns to the profile page and forgets the checkbox`() {
        acknowledgedWarning()

        viewModel.onDismiss()

        assertEquals(DeleteAccountState(), viewModel.state.value)
    }

    @Test
    fun `the checkbox is ignored outside the warning`() {
        viewModel.onAcknowledgedChange(true)

        assertEquals(DeleteAccountState(), viewModel.state.value)
    }
    // endregion

    // region sign in again
    @Test
    fun `Continue with the checkbox on opens the sign-in page`() {
        acknowledgedWarning()

        assertEquals(browserIntent, viewModel.reauthenticationIntent())
        assertEquals(DeleteAccountStep.SigningIn, step())
    }

    @Test
    fun `a cancelled sign-in returns to the profile page and deletes nothing`() = runTest {
        acknowledgedWarning()
        viewModel.reauthenticationIntent()

        viewModel.onReauthenticationResult(null)

        assertEquals(DeleteAccountState(), viewModel.state.value)
        coVerify(exactly = 0) { accountDeletionService.deleteAccount() }
    }

    @Test
    fun `the same account signing in again leads to the final dialog`() = runTest {
        signedInAgain()

        assertEquals(DeleteAccountStep.Confirm, step())
        coVerify(exactly = 0) { accountDeletionService.deleteAccount() }
    }

    @Test
    fun `a different account stops the flow with a message and deletes nothing`() = runTest {
        coEvery { authService.completeReauthentication(redirectData) } returns ReauthenticationOutcome.DifferentAccount
        val messages = mutableListOf<UiText>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.snackbarMessages.toList(messages) }

        signedInAgain()

        assertEquals(DeleteAccountState(), viewModel.state.value)
        assertEquals(listOf<UiText>(UiText.Resource(ResStrings.deleteAccountErrorDifferentAccount)), messages)
        coVerify(exactly = 0) { accountDeletionService.deleteAccount() }
    }

    @Test
    fun `a failed sign-in stops the flow with a message`() = runTest {
        coEvery { authService.completeReauthentication(redirectData) } returns ReauthenticationOutcome.Failed
        val messages = mutableListOf<UiText>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.snackbarMessages.toList(messages) }

        signedInAgain()

        assertEquals(DeleteAccountState(), viewModel.state.value)
        assertEquals(listOf<UiText>(UiText.Resource(ResStrings.deleteAccountErrorSignIn)), messages)
    }

    @Test
    fun `a sign-in result that nobody waits for is ignored`() = runTest {
        viewModel.onReauthenticationResult(redirectData)

        assertEquals(DeleteAccountState(), viewModel.state.value)
        coVerify(exactly = 0) { authService.completeReauthentication(any()) }
    }
    // endregion

    // region final dialog and the delete call
    @Test
    fun `Cancel on the final dialog returns to the profile page`() = runTest {
        signedInAgain()

        viewModel.onDismiss()

        assertEquals(DeleteAccountState(), viewModel.state.value)
        coVerify(exactly = 0) { accountDeletionService.deleteAccount() }
    }

    @Test
    fun `Delete calls the service once and keeps the progress dialog for the shell to replace`() = runTest {
        signedInAgain()

        viewModel.onConfirmDelete()
        viewModel.onConfirmDelete()

        assertEquals(DeleteAccountStep.Deleting, step())
        coVerify(exactly = 1) { accountDeletionService.deleteAccount() }
    }

    @Test
    fun `Delete is ignored before the sign-in is done`() = runTest {
        acknowledgedWarning()

        viewModel.onConfirmDelete()

        coVerify(exactly = 0) { accountDeletionService.deleteAccount() }
    }

    @Test
    fun `an old login goes back to the warning with a message and is not retried`() = runTest {
        coEvery { accountDeletionService.deleteAccount() } throws ReauthenticationRequiredException()
        val messages = mutableListOf<UiText>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.snackbarMessages.toList(messages) }
        signedInAgain()

        viewModel.onConfirmDelete()

        assertEquals(DeleteAccountState(DeleteAccountStep.Warning, acknowledged = true), viewModel.state.value)
        assertEquals(listOf<UiText>(UiText.Resource(ResStrings.deleteAccountErrorReauth)), messages)
        coVerify(exactly = 1) { accountDeletionService.deleteAccount() }
    }

    @Test
    fun `an unknown result returns to the final dialog so Delete can try again`() = runTest {
        coEvery { accountDeletionService.deleteAccount() } throws RuntimeException("timeout")
        val messages = mutableListOf<UiText>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.snackbarMessages.toList(messages) }
        signedInAgain()

        viewModel.onConfirmDelete()

        assertEquals(DeleteAccountStep.Confirm, step())
        assertEquals(listOf<UiText>(UiText.Resource(ResStrings.deleteAccountErrorUnknown)), messages)

        coEvery { accountDeletionService.deleteAccount() } returns Unit
        viewModel.onConfirmDelete()
        coVerify(exactly = 2) { accountDeletionService.deleteAccount() }
    }
    // endregion
}
