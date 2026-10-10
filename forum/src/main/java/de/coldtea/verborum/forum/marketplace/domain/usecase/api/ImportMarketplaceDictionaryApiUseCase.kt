package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceImportResult
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * `POST /marketplace/dictionaries/{id}/import` (guide §5.3). Idempotent, so a 500 — the
 * double-import race the guide describes — is retried once. The caller hides the action on the
 * user's own listings, so the 400 for a self-import is reported as a plain failure.
 */
class ImportMarketplaceDictionaryApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(dictionaryId: String): MarketplaceImportResult {
        var code = post(dictionaryId)
        if (code == HTTP_SERVER_ERROR) code = post(dictionaryId)
        return when {
            code == null -> MarketplaceImportResult.Failed
            code in 200..299 -> MarketplaceImportResult.Imported
            code == HTTP_FORBIDDEN -> MarketplaceImportResult.NotMember
            code == HTTP_NOT_FOUND -> MarketplaceImportResult.Unavailable
            else -> MarketplaceImportResult.Failed
        }
    }

    /** The status code, or null when the server was not reached. */
    private suspend fun post(dictionaryId: String): Int? =
        try {
            repository.importDictionary(dictionaryId).code()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private companion object {
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_SERVER_ERROR = 500
    }
}
