package de.coldtea.verborum.forum.marketplace.ui.model

import de.coldtea.verborum.core.ui.UiText
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceImportResult
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumUserState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/**
 * The download bookkeeping both Forum screens share: what is in flight, what this session
 * downloaded, and what turned out to be unavailable — merged with the downloads already on the
 * device into a [ForumUserState]. Owned by a ViewModel; the ViewModel launches [download].
 */
class ForumDownloads(private val marketplaceService: MarketplaceService) {

    private val downloading = MutableStateFlow<Set<String>>(emptySet())
    // Shown as downloaded at once, even before (or without) the local copy landing.
    private val downloadedThisSession = MutableStateFlow<Set<String>>(emptySet())
    private val unavailable = MutableStateFlow<Set<String>>(emptySet())

    val userState: Flow<ForumUserState> = combine(
        marketplaceService.observeDownloadedDictionaryIds(),
        downloadedThisSession,
        downloading,
        unavailable,
    ) { stored, session, inFlight, gone ->
        ForumUserState(
            userId = marketplaceService.currentUserId(),
            downloadedIds = stored + session,
            downloadingIds = inFlight,
            unavailableIds = gone,
        )
    }

    /**
     * Downloads [dictionary] unless it is the user's own, already downloaded, or already in flight
     * (guarding the double-tap race, guide §3 rule 5). Returns the message to show, or null when
     * nothing was attempted; [MarketplaceImportResult.NotMember] is reported through [onNotMember].
     */
    suspend fun download(dictionary: ForumDictionaryUi, onNotMember: () -> Unit): UiText? {
        if (!dictionary.canDownload || dictionary.dictionaryId in downloading.value) return null
        downloading.update { it + dictionary.dictionaryId }
        val result = try {
            marketplaceService.downloadDictionary(dictionary)
        } finally {
            downloading.update { it - dictionary.dictionaryId }
        }
        return when (result) {
            MarketplaceImportResult.Imported -> {
                downloadedThisSession.update { it + dictionary.dictionaryId }
                UiText.Resource(ResStrings.forumDownloadSucceeded)
            }
            MarketplaceImportResult.Unavailable -> {
                unavailable.update { it + dictionary.dictionaryId }
                UiText.Resource(ResStrings.forumDownloadUnavailable)
            }
            MarketplaceImportResult.NotMember -> {
                onNotMember()
                UiText.Resource(ResStrings.forumDownloadFailed)
            }
            MarketplaceImportResult.Failed -> UiText.Resource(ResStrings.forumDownloadFailed)
        }
    }
}
