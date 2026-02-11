package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model

import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi

sealed class DictionaryDetailState{
    data object Failed: DictionaryDetailState()
    data object Loading: DictionaryDetailState()

    data class Success(val dictionaryUi: DictionaryUi, val wordsUi: List<WordUi>) : DictionaryDetailState()
}
