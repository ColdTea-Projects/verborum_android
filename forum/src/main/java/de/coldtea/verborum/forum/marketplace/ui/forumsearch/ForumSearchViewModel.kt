package de.coldtea.verborum.forum.marketplace.ui.forumsearch

import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * The Forum search screen's draft: edited freely here and handed back to the list only on Search,
 * so leaving with Back changes nothing. Needs no data, so it talks to no Service.
 */
@HiltViewModel
class ForumSearchViewModel @Inject constructor() : BaseViewModel() {

    private val _draft = MutableStateFlow(ForumDictionaryFilter())
    val draft = _draft.asStateFlow()

    private var initialized = false

    /** Starts the draft from the list's current filter — once, so a rotation keeps the edits. */
    fun init(current: ForumDictionaryFilter) {
        if (initialized) return
        initialized = true
        _draft.value = current
    }

    /** Adds the pair at the end, unless the same two languages are already there in either order. */
    fun onAddLanguagePair(first: SupportedLanguage, second: SupportedLanguage) = _draft.update { draft ->
        val pair = ForumLanguagePair(first = first, second = second)
        if (first == second || draft.languagePairs.any { it.isSamePairAs(pair) }) draft
        else draft.copy(languagePairs = draft.languagePairs + pair)
    }

    fun onRemoveLanguagePair(pair: ForumLanguagePair) = _draft.update { draft ->
        draft.copy(languagePairs = draft.languagePairs - pair)
    }

    fun onToggleTag(code: String) = _draft.update { draft ->
        draft.copy(tags = if (code in draft.tags) draft.tags - code else draft.tags + code)
    }

    /** Keeps the text as typed; it is trimmed when the search is applied. */
    fun onUserNameChange(userName: String) = _draft.update { it.copy(userName = userName) }

    fun clear() {
        _draft.value = ForumDictionaryFilter()
    }

    /** The draft as the list applies it: a blank user name means no user filter. */
    fun searchFilter(): ForumDictionaryFilter =
        _draft.value.let { draft -> draft.copy(userName = draft.userName?.trim()?.ifBlank { null }) }
}
