package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

sealed class ForumDictionaryListState {
    data object Loading : ForumDictionaryListState()
    data object Failed : ForumDictionaryListState()

    data class Success(val dictionaries: List<ForumDictionaryUi>) : ForumDictionaryListState()
}
