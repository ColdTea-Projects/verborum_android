package de.coldtea.verborum.forum.marketplace.domain

import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceWord
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingsApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceWordsApiUseCase
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumWordUi
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import javax.inject.Inject

/**
 * The only marketplace API the Forum ViewModels talk to. One-shot reads, not flows: the marketplace
 * is online-only (guide §5.6), so there is no local copy to observe.
 */
class MarketplaceService @Inject constructor(
    private val getMarketplaceListingsApiUseCase: GetMarketplaceListingsApiUseCase,
    private val getMarketplaceListingApiUseCase: GetMarketplaceListingApiUseCase,
    private val getMarketplaceWordsApiUseCase: GetMarketplaceWordsApiUseCase,
) {
    suspend fun getDictionaries(): List<ForumDictionaryUi> =
        getMarketplaceListingsApiUseCase().map(MarketplaceListing::convertToUi)

    suspend fun getDictionary(dictionaryId: String): ForumDictionaryUi? =
        getMarketplaceListingApiUseCase(dictionaryId)?.convertToUi()

    suspend fun getWords(dictionaryId: String): List<ForumWordUi> =
        getMarketplaceWordsApiUseCase(dictionaryId).map(MarketplaceWord::convertToUi)
}
