package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model

import de.coldtea.verborum.core.ui.model.DictionaryRatingUi
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi

sealed class ForumDictionaryDetailState {
    data object Loading : ForumDictionaryDetailState()
    data object Failed : ForumDictionaryDetailState()

    data class Success(
        val dictionary: ForumDictionaryUi,
        val words: List<ForumWordUi>,
        // The user's own rating — only for a dictionary they downloaded and do not own, once it
        // loaded; null otherwise.
        val rating: DictionaryRatingUi? = null,
    ) : ForumDictionaryDetailState()
}
