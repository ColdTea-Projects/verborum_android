package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model

import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.core.ui.model.DictionaryRatingUi

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
        // Only for a Forum member's own dictionary; null otherwise (a download, or not a member).
        val sharing: DictionarySharingUi? = null,
        // The user's own rating — only for a downloaded dictionary, once it loaded; null otherwise.
        val rating: DictionaryRatingUi? = null,
    ) : DictionaryDetailState()
}
