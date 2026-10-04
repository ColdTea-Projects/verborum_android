package de.coldtea.verborum.core.profile.ui.profile.model

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(val form: ProfileForm) : ProfileState()
    data object Failed : ProfileState()
}
