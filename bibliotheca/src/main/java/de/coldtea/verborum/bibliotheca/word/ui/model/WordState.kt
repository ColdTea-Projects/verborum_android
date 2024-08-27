package de.coldtea.verborum.bibliotheca.word.ui.model

sealed class WordState {
    data object Failed: WordState()
    data object Loading: WordState()
    data class Success(val wordList: List<WordUi>): WordState()
}

