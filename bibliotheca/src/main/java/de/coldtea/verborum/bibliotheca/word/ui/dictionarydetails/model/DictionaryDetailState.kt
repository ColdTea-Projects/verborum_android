package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model

import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi

sealed class DictionaryDetailState{
    data object Failed: DictionaryDetailState()
    data object Loading: DictionaryDetailState()

    data object Deleted: DictionaryDetailState()

    /**
     * [canSelfPractice] needs at least one word here; [canTest] additionally needs enough distinct
     * entries across the whole language pair to build multiple-choice distractors. Both are decided
     * in the view model so the screen only has to render them.
     */
    data class Success(
        val dictionaryUi: DictionaryUi,
        val wordsUi: List<WordUi>,
        val canSelfPractice: Boolean = false,
        val canTest: Boolean = false,
    ) : DictionaryDetailState()
}
