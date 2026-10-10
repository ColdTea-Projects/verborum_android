package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumDictionaryDetailState
import de.coldtea.verborum.forum.marketplace.ui.model.ForumDownloads
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryDetailsViewModel @Inject constructor(
    private val marketplaceService: MarketplaceService,
) : BaseViewModel() {

    private val _dictionaryDetailState =
        MutableStateFlow<ForumDictionaryDetailState>(ForumDictionaryDetailState.Loading)

    private val downloads = ForumDownloads(marketplaceService)

    /** The loaded dictionary, marked as the user's own / downloaded / downloading. */
    val dictionaryDetailState: StateFlow<ForumDictionaryDetailState> =
        combine(_dictionaryDetailState, downloads.userState) { state, userState ->
            if (state is ForumDictionaryDetailState.Success) {
                state.copy(dictionary = userState.applyTo(state.dictionary))
            } else {
                state
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ForumDictionaryDetailState.Loading)

    private var loadJob: Job? = null
    private var dictionaryId: String = ""

    fun init(dictionaryId: String) {
        this.dictionaryId = dictionaryId
        loadDetails()
    }

    fun retry() = loadDetails()

    /** Downloads the shown dictionary into the user's Bibliotheca. */
    fun download() {
        val dictionary = (dictionaryDetailState.value as? ForumDictionaryDetailState.Success)?.dictionary ?: return
        viewModelScope.launch(exceptionHandler) {
            downloads.download(dictionary, onNotMember = {})?.let { _snackbarMessages.emit(it) }
        }
    }

    private fun loadDetails() {
        loadJob?.cancel()
        _dictionaryDetailState.value = ForumDictionaryDetailState.Loading
        loadJob = viewModelScope.launch(exceptionHandler) {
            _dictionaryDetailState.value = try {
                coroutineScope {
                    val dictionary = async { marketplaceService.getDictionary(dictionaryId) }
                    val words = async { marketplaceService.getWords(dictionaryId) }
                    // A dictionary made private or deleted since the list loaded (404,
                    // guide §6.7) has nothing to show, so it reads as a failed load.
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
