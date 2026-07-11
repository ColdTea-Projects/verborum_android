package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi

sealed class CreateWordState {
    data object Loading : CreateWordState()
    data object Failed : CreateWordState()

    /** [editingWord] is set when the screen was opened with a wordId — edit mode. */
    data class Success(
        val dictionaryUi: DictionaryUi,
        val editingWord: WordUi? = null,
    ) : CreateWordState()
}
