package de.coldtea.verborum.bibliotheca.word.ui.selfpractice.model

import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi

sealed class SelfPracticeState{
    data object Failed: SelfPracticeState()
    data object Loading: SelfPracticeState()

    data class Success(val dictionaryUi: DictionaryUi, val wordsUi: List<WordUi>) : SelfPracticeState()
}
