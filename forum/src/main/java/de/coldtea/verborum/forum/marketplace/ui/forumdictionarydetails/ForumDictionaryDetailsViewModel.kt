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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.core.ui.model.DictionaryRatingUi
import de.coldtea.verborum.core.ui.model.errorMessage
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceRatingService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumDictionaryDetailsViewModel @Inject constructor(
    private val marketplaceService: MarketplaceService,
    private val ratingService: MarketplaceRatingService,
) : BaseViewModel() {

    private val _dictionaryDetailState =
        MutableStateFlow<ForumDictionaryDetailState>(ForumDictionaryDetailState.Loading)

    private val downloads = ForumDownloads(marketplaceService)

    // The user's own rating; null until it is loaded (or if it could not be), which hides the control.
    private val myRating = MutableStateFlow<DictionaryRatingUi?>(null)

    /**
     * The loaded dictionary, marked as the user's own / downloaded / downloading — with the user's
     * rating of it, shown only once they have downloaded it (guide §6.10 rule 3).
     */
    val dictionaryDetailState: StateFlow<ForumDictionaryDetailState> =
        combine(_dictionaryDetailState, downloads.userState, myRating) { state, userState, rating ->
            if (state is ForumDictionaryDetailState.Success) {
                val dictionary = userState.applyTo(state.dictionary)
                state.copy(
                    dictionary = dictionary,
                    rating = rating.takeIf { dictionary.isDownloaded && !dictionary.isOwn },
                )
            } else {
                state
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ForumDictionaryDetailState.Loading)

    private var loadJob: Job? = null
    private var dictionaryId: String = ""

    init {
        // The rating is read once the dictionary is known to be downloaded — also when the user
        // downloads it on this very screen.
        viewModelScope.launch(exceptionHandler) {
            dictionaryDetailState
                .map { state ->
                    (state as? ForumDictionaryDetailState.Success)?.dictionary
                        ?.let { it.isDownloaded && !it.isOwn } == true
                }
                .first { it }
            loadRating()
        }
    }

    fun init(dictionaryId: String) {
        this.dictionaryId = dictionaryId
        loadDetails()
    }

    fun retry() = loadDetails()

    private suspend fun loadRating() {
        try {
            myRating.value = DictionaryRatingUi(stars = ratingService.getMyRating(dictionaryId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline: no control rather than a wrong "not rated" — it is offered again next time.
        }
    }

    /**
     * A tap on a star: rates with it, or — on the star already chosen — removes the rating
     * (guide §6.10). Online-only; a failure keeps the previous rating and says why.
     */
    fun rate(stars: Int) {
        val current = myRating.value ?: return
        if (current.isUpdating) return
        val removing = current.stars == stars

        myRating.value = current.copy(isUpdating = true)
        viewModelScope.launch(exceptionHandler) {
            val outcome = if (removing) ratingService.removeRating(dictionaryId)
            else ratingService.setRating(dictionaryId, stars)

            if (outcome == RatingOutcome.DONE) {
                myRating.value = DictionaryRatingUi(stars = stars.takeUnless { removing })
            } else {
                myRating.value = current
                outcome.errorMessage()?.let { _snackbarMessages.emit(it) }
            }
        }
    }

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
