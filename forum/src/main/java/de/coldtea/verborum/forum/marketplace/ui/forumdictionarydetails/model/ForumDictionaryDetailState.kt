package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model

import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi

sealed class ForumDictionaryDetailState {
    data object Loading : ForumDictionaryDetailState()
    data object Failed : ForumDictionaryDetailState()

    data class Success(
        val dictionary: ForumDictionaryUi,
        val words: List<ForumWordUi>,
    ) : ForumDictionaryDetailState()
}
