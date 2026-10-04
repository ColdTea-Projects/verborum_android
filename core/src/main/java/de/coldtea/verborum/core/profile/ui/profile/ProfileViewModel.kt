package de.coldtea.verborum.core.profile.ui.profile

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.profile.domain.ProfileService
import de.coldtea.verborum.core.profile.domain.model.InvalidProfileException
import de.coldtea.verborum.core.profile.domain.usecase.api.EnsureUserProfileUseCase
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileForm
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileState
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
import de.coldtea.verborum.core.utils.ResStrings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileService: ProfileService,
) : BaseViewModel() {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        // Every newly loaded profile (first load, or the reload after an update) resets the form
        // to the saved values.
        profileService.observeProfile().observe(
            onSuccess = { profile ->
                profile?.let { _state.value = ProfileState.Success(ProfileForm.from(it)) }
            },
        )
        refresh()
    }

    /** Always reloads from `/users/me` on entry — the profile may have changed on another device. */
    fun refresh() {
        if (_state.value is ProfileState.Failed) _state.value = ProfileState.Loading
        viewModelScope.launch(exceptionHandler) {
            try {
                profileService.refreshProfile()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // A cached profile stays editable; only a first load that fails is a screen error.
                if (_state.value !is ProfileState.Success) _state.value = ProfileState.Failed
            }
        }
    }

    fun onDisplayNameChange(displayName: String) = updateForm {
        it.copy(displayName = displayName.take(EnsureUserProfileUseCase.MAX_DISPLAY_NAME_LENGTH))
    }

    fun onAgreementChange(accepted: Boolean) = updateForm { it.copy(agreementAccepted = accepted) }

    /** Sends only the fields that differ from the saved profile (a null one is left unchanged). */
    fun update() {
        val form = (_state.value as? ProfileState.Success)?.form ?: return
        if (!form.canUpdate) return
        updateForm { it.copy(isSaving = true) }

        viewModelScope.launch(exceptionHandler) {
            try {
                profileService.updateProfile(
                    displayName = form.trimmedDisplayName.takeIf { it != form.savedDisplayName },
                    marketplaceAgreementAccepted =
                        form.agreementAccepted.takeIf { it != form.savedAgreementAccepted },
                )
                _snackbarMessages.emit(UiText.Resource(ResStrings.profileUpdated))
            } catch (e: CancellationException) {
                throw e
            } catch (e: InvalidProfileException) {
                // The server's errorDetail is written for people (guide §6.2).
                _snackbarMessages.emit(
                    e.detail?.let(UiText::Dynamic) ?: UiText.Resource(ResStrings.profileUpdateError)
                )
            } catch (e: Exception) {
                _snackbarMessages.emit(UiText.Resource(ResStrings.profileUpdateError))
            } finally {
                updateForm { it.copy(isSaving = false) }
            }
        }
    }

    private fun updateForm(transform: (ProfileForm) -> ProfileForm) {
        _state.update { state ->
            if (state is ProfileState.Success) state.copy(form = transform(state.form)) else state
        }
    }
}
