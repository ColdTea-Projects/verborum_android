package de.coldtea.verborum.core.profile.ui.deleteaccount.model

/**
 * Where the person is in the delete-account flow (`docs/account-deletion-guide.md` §3).
 * The letters match the guide's diagram.
 */
sealed class DeleteAccountStep {
    /** The profile page, nothing started. */
    data object Idle : DeleteAccountStep()

    /** [A] The warning with the "I understand" checkbox. */
    data object Warning : DeleteAccountStep()

    /** [B] The browser shows the sign-in page, and the result has not come back yet. */
    data object SigningIn : DeleteAccountStep()

    /** [B/C] Exchanging the code and checking that the account is the same. */
    data object Verifying : DeleteAccountStep()

    /** [D] The final "Delete permanently?" dialog. */
    data object Confirm : DeleteAccountStep()

    /** [E] The server call is running. The shell shows the login wall when it succeeds. */
    data object Deleting : DeleteAccountStep()
}

data class DeleteAccountState(
    val step: DeleteAccountStep = DeleteAccountStep.Idle,
    val acknowledged: Boolean = false,
)
