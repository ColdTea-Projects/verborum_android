package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi

sealed class CreateWordState {
    data object Loading : CreateWordState()
    data object Failed : CreateWordState()

    data class Success(val dictionaryUi: DictionaryUi) : CreateWordState()
}
