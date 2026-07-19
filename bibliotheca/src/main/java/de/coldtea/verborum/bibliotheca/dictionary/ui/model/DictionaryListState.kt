package de.coldtea.verborum.bibliotheca.dictionary.ui.model

sealed class DictionaryListState {
    data object Loading : DictionaryListState()
    data object Failed : DictionaryListState()

    data class Success(val dictionaries: List<DictionaryUi>) : DictionaryListState()
}
