package de.coldtea.verborum.core.options.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.options.domain.AppLanguageService
import de.coldtea.verborum.core.options.ui.model.AppLanguage
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val appLanguageService: AppLanguageService,
) : BaseViewModel() {

    // null = follow the device language.
    private val _selected = MutableStateFlow(appLanguageService.selected())
    val selected: StateFlow<AppLanguage?> = _selected.asStateFlow()

    /**
     * Applying a language recreates the activity, so this state update mostly serves the frame
     * before that happens — and the re-created screen reads the persisted value back anyway.
     */
    fun select(language: AppLanguage?) {
        if (language == _selected.value) return
        _selected.value = language
        appLanguageService.select(language)
    }
}
