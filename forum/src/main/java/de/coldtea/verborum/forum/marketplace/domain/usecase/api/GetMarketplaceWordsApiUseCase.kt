package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceWordResponse
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceWord
import javax.inject.Inject

class GetMarketplaceWordsApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(dictionaryId: String): List<MarketplaceWord> =
        repository.getWords(dictionaryId).map(MarketplaceWordResponse::convertToWord)
}
