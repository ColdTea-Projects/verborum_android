package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionaryDetailState
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.deletionMessage
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.REQUIRED_WORDS_FOR_TEST
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.core.ui.model.DictionaryRatingUi
import de.coldtea.verborum.core.ui.model.errorMessage
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.SharingChange
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionarySharingUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryDetailsViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val wordService: WordService,
) : BaseViewModel() {

    private val _dictionaryDetailState =
        MutableStateFlow<DictionaryDetailState>(DictionaryDetailState.Loading)
    val dictionaryDetailState = _dictionaryDetailState.asSharedFlow()

    private var loadJob: Job? = null
    private var dictionaryId: String = ""

    private val updatingSharing = MutableStateFlow(false)

    // The user's own rating; null until it is loaded (or if it could not be), which hides the control.
    private val myRating = MutableStateFlow<DictionaryRatingUi?>(null)
    private var ratingRequested = false

    fun init(dictionaryId: String) {
        this.dictionaryId = dictionaryId
        observeDetails()
        // Membership decides whether the sharing switch shows; load it if this session has not.
        viewModelScope.launch(exceptionHandler) { dictionaryService.refreshMembership() }
    }

    /** Re-subscribes after a Failed load (the observed flow terminates on error). */
    fun retry() {
        _dictionaryDetailState.tryEmit(DictionaryDetailState.Loading)
        observeDetails()
    }

    private fun observeDetails() {
        loadJob?.cancel()
        val sharingAndRating = combine(
            dictionaryService.observeSharingState(),
            updatingSharing,
            myRating,
        ) { sharingState, isUpdatingSharing, rating -> Triple(sharingState, isUpdatingSharing, rating) }

        loadJob = combine(
            dictionaryService.observeDictionary(dictionaryId),
            wordService.observeWordsByDictionary(dictionaryId),
            wordService.observeWordsInLanguagePair(dictionaryId),
            sharingAndRating,
        ) { dictionary, words, languagePairWords, (sharingState, isUpdatingSharing, rating) ->
            // A null dictionary means it was tombstoned/removed — surface it as Deleted so the
            // screen navigates back instead of re-rendering (and re-registering) a stale header.
            if (dictionary == null) {
                DictionaryDetailState.Deleted
            } else {
                val distinctInPair =
                    languagePairWords.distinctBy { it.word + it.translation }.size
                DictionaryDetailState.Success(
                    dictionaryUi = dictionary,
                    wordsUi = words,
                    canSelfPractice = words.isNotEmpty(),
                    canTest = words.isNotEmpty() && distinctInPair >= REQUIRED_WORDS_FOR_TEST,
                    sharing = if (dictionary.isImported || !sharingState.isMember) null else DictionarySharingUi(
                        isShared = dictionary.isPublic,
                        isLastShared = sharingState.isLastShared(dictionary.dictionaryId),
                        isUpdating = isUpdatingSharing,
                    ),
                    rating = rating.takeIf { dictionary.isImported },
                )
            }
        }.observe (
            onSuccess = { state ->
                _dictionaryDetailState.emit(state)
                // A downloaded dictionary can be rated; read the user's rating once.
                if (state is DictionaryDetailState.Success && state.dictionaryUi.isImported && !ratingRequested) {
                    ratingRequested = true
                    loadRating()
                }
            },
            onError = {
                _dictionaryDetailState.emit(DictionaryDetailState.Failed)
            }
        )
    }

    fun deleteWord(wordId: String) = viewModelScope.launch {
        try {
            wordService.deleteWord(wordId)
        } catch (e: Exception) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorDeleteFailed))
        }
    }

    /**
     * Shares or unshares the shown dictionary in the Forum. The member's last shared dictionary
     * cannot be unshared — the switch is locked, and the server would refuse it anyway.
     */
    fun setShared(isPublic: Boolean) {
        val sharing = (_dictionaryDetailState.value as? DictionaryDetailState.Success)?.sharing ?: return
        if (!sharing.canToggle || sharing.isShared == isPublic) return

        updatingSharing.value = true
        viewModelScope.launch(exceptionHandler) {
            try {
                when (dictionaryService.setShared(dictionaryId, isPublic)) {
                    SharingChange.UPDATED -> Unit
                    SharingChange.LAST_SHARED -> explainLastShared()
                    SharingChange.FAILED -> _snackbarMessages.emit(UiText.Resource(ResStrings.errorSharingFailed))
                }
            } finally {
                updatingSharing.value = false
            }
        }
    }

    private fun loadRating() {
        viewModelScope.launch(exceptionHandler) {
            try {
                myRating.value = DictionaryRatingUi(stars = dictionaryService.getMyRating(dictionaryId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Offline: no control rather than a wrong "not rated" — it is offered again next time.
                ratingRequested = false
            }
        }
    }

    /**
     * A tap on a star: rates with it, or — on the star already chosen — removes the rating
     * (marketplace guide §6.10). Online-only; a failure keeps the previous rating and says why.
     */
    fun rate(stars: Int) {
        val current = myRating.value ?: return
        if (current.isUpdating) return
        val removing = current.stars == stars

        myRating.value = current.copy(isUpdating = true)
        viewModelScope.launch(exceptionHandler) {
            val outcome = if (removing) dictionaryService.removeRating(dictionaryId)
            else dictionaryService.setRating(dictionaryId, stars)

            if (outcome == RatingOutcome.DONE) {
                myRating.value = DictionaryRatingUi(stars = stars.takeUnless { removing })
            } else {
                myRating.value = current
                outcome.errorMessage()?.let { _snackbarMessages.emit(it) }
            }
        }
    }

    /** Why the member's last shared dictionary can be neither unshared nor deleted. */
    fun explainLastShared() {
        viewModelScope.launch(exceptionHandler) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorDeleteLastShared))
        }
    }

    /**
     * Unsubscribes from a downloaded dictionary. Once it is gone locally the observed dictionary
     * turns null, the state becomes Deleted and the screen leaves — as after a delete.
     */
    fun unsubscribe() = viewModelScope.launch {
        val state = _dictionaryDetailState.value as? DictionaryDetailState.Success ?: return@launch
        try {
            dictionaryService.unsubscribe(state.dictionaryUi.dictionaryId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorUnsubscribeFailed))
        }
    }

    /**
     * Deletes the dictionary — or, offline, tombstones it for sync to finish; either way it goes
     * away locally, the state becomes Deleted and the screen leaves. A refusal by the server (the
     * member's last shared dictionary) keeps the screen and says why.
     */
    fun deleteDictionary() = viewModelScope.launch {
        val state = _dictionaryDetailState.value as? DictionaryDetailState.Success ?: return@launch

        try {
            deletionMessage(dictionaryService.deleteDictionary(state.dictionaryUi.dictionaryId))
                ?.let { _snackbarMessages.emit(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorDeleteFailed))
        }
    }
}