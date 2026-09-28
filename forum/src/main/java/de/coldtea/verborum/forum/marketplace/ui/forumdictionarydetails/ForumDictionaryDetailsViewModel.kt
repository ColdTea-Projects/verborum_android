package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumDictionaryDetailState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryDetailsViewModel @Inject constructor(
    private val marketplaceService: MarketplaceService,
) : BaseViewModel() {

    private val _dictionaryDetailState =
        MutableStateFlow<ForumDictionaryDetailState>(ForumDictionaryDetailState.Loading)
    val dictionaryDetailState = _dictionaryDetailState.asStateFlow()

    private var loadJob: Job? = null
    private var dictionaryId: String = ""

    fun init(dictionaryId: String) {
        this.dictionaryId = dictionaryId
        loadDetails()
    }

    fun retry() = loadDetails()

    private fun loadDetails() {
        loadJob?.cancel()
        _dictionaryDetailState.value = ForumDictionaryDetailState.Loading
        loadJob = viewModelScope.launch(exceptionHandler) {
            _dictionaryDetailState.value = try {
                coroutineScope {
                    val dictionary = async { marketplaceService.getDictionary(dictionaryId) }
                    val words = async { marketplaceService.getWords(dictionaryId) }
                    // An unknown id (once real: unpublished or deleted since the list loaded,
                    // guide §4.3) has nothing to show, so it reads as a failed load.
                    dictionary.await()
                        ?.let { ForumDictionaryDetailState.Success(it, words.await()) }
                        ?: ForumDictionaryDetailState.Failed
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ForumDictionaryDetailState.Failed
            }
        }
    }
}
