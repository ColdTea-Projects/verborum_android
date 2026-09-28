package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryListState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryListViewModel @Inject constructor(
    private val marketplaceService: MarketplaceService,
) : BaseViewModel() {

    private val _dictionariesState =
        MutableStateFlow<ForumDictionaryListState>(ForumDictionaryListState.Loading)
    val dictionariesState = _dictionariesState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadDictionaries()
    }

    fun retry() = loadDictionaries()

    private fun loadDictionaries() {
        loadJob?.cancel()
        _dictionariesState.value = ForumDictionaryListState.Loading
        loadJob = viewModelScope.launch(exceptionHandler) {
            _dictionariesState.value = try {
                ForumDictionaryListState.Success(marketplaceService.getDictionaries())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ForumDictionaryListState.Failed
            }
        }
    }
}
