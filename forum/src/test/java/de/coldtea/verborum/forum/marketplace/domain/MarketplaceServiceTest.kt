package de.coldtea.verborum.forum.marketplace.domain

import androidx.paging.testing.asSnapshot
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import de.coldtea.verborum.core.profile.domain.ProfileService
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceLanguagePair
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingFilter
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingOrder
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingPage
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingsApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceWordsApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.local.GetOwnedDictionaryLanguagesUseCase
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair
import de.coldtea.verborum.forum.testForumDictionaryUi
import de.coldtea.verborum.forum.testForumWordUi
import de.coldtea.verborum.forum.testMarketplaceListing
import de.coldtea.verborum.forum.testMarketplaceWord
import de.coldtea.verborum.forum.vault.domain.usecase.api.GetVaultDictionaryLanguagesApiUseCase
import de.coldtea.verborum.core.auth.domain.usecase.GetActiveUserUseCase
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceImportResult
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.ImportMarketplaceDictionaryApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.local.ObserveDownloadedDictionaryIdsUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.local.SaveDownloadedDictionaryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarketplaceServiceTest : BaseTest() {

    @MockK
    private lateinit var getMarketplaceListingsApiUseCase: GetMarketplaceListingsApiUseCase

    @MockK
    private lateinit var getMarketplaceListingApiUseCase: GetMarketplaceListingApiUseCase

    @MockK
    private lateinit var getMarketplaceWordsApiUseCase: GetMarketplaceWordsApiUseCase

    @MockK
    private lateinit var getOwnedDictionaryLanguagesUseCase: GetOwnedDictionaryLanguagesUseCase

    @MockK
    private lateinit var getVaultDictionaryLanguagesApiUseCase: GetVaultDictionaryLanguagesApiUseCase

    @MockK
    private lateinit var profileService: ProfileService

    @MockK
    private lateinit var importMarketplaceDictionaryApiUseCase: ImportMarketplaceDictionaryApiUseCase

    @MockK
    private lateinit var observeDownloadedDictionaryIdsUseCase: ObserveDownloadedDictionaryIdsUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var saveDownloadedDictionaryUseCase: SaveDownloadedDictionaryUseCase

    @MockK
    private lateinit var getActiveUserUseCase: GetActiveUserUseCase

    private lateinit var subject: MarketplaceService

    override fun setUp() {
        super.setUp()
        subject = MarketplaceService(
            getMarketplaceListingsApiUseCase,
            getMarketplaceListingApiUseCase,
            getMarketplaceWordsApiUseCase,
            getOwnedDictionaryLanguagesUseCase,
            getVaultDictionaryLanguagesApiUseCase,
            profileService,
            importMarketplaceDictionaryApiUseCase,
            observeDownloadedDictionaryIdsUseCase,
            saveDownloadedDictionaryUseCase,
            getActiveUserUseCase,
        )
    }

    // region browse
    @Test
    fun `getDictionaries pages listings as UI models, import count as downloads`() = runTest {
        coEvery {
            getMarketplaceListingsApiUseCase(
                MarketplaceListingOrder.NEWEST,
                0,
                MarketplaceService.LISTING_PAGE_SIZE,
                MarketplaceListingFilter(),
            )
        } returns MarketplaceListingPage(
            listings = listOf(
                testMarketplaceListing(dictionaryId = "a", importCount = 42),
                testMarketplaceListing(dictionaryId = "b", publisherName = null),
            ),
            page = 0,
            hasMore = false,
        )

        assertEquals(
            listOf(
                testForumDictionaryUi(dictionaryId = "a", downloadCount = 42),
                testForumDictionaryUi(dictionaryId = "b", publisherName = null),
            ),
            subject.getDictionaries(ForumDictionaryFilter(), MarketplaceListingOrder.NEWEST).asSnapshot(),
        )
    }

    @Test
    fun `getDictionaries sends the order and the filter with uppercase wire language codes`() = runTest {
        coEvery {
            getMarketplaceListingsApiUseCase(any(), any(), any(), any())
        } returns MarketplaceListingPage(listings = emptyList(), page = 0, hasMore = false)

        subject.getDictionaries(
            ForumDictionaryFilter(
                languagePairs = listOf(ForumLanguagePair(SupportedLanguage.ENGLISH, SupportedLanguage.GERMAN)),
                tags = setOf("a1", "travel"),
                userName = "anna",
            ),
            MarketplaceListingOrder.POPULAR,
        ).asSnapshot()

        coVerify {
            getMarketplaceListingsApiUseCase(
                MarketplaceListingOrder.POPULAR,
                0,
                MarketplaceService.LISTING_PAGE_SIZE,
                MarketplaceListingFilter(
                    languagePairs = listOf(MarketplaceLanguagePair("EN", "DE")),
                    tags = setOf("a1", "travel"),
                    publisherName = "anna",
                ),
            )
        }
    }

    @Test
    fun `getDictionary returns null for an unknown dictionary`() = runTest {
        coEvery { getMarketplaceListingApiUseCase("missing") } returns null

        assertNull(subject.getDictionary("missing"))
    }

    @Test
    fun `getWords joins alternatives with a slash`() = runTest {
        coEvery { getMarketplaceWordsApiUseCase("dictionaryId") } returns listOf(
            testMarketplaceWord(surfaces = listOf("buy", "purchase"), translations = listOf("kaufen", "erwerben")),
        )

        assertEquals(
            listOf(testForumWordUi(word = "buy/purchase", translation = "kaufen/erwerben")),
            subject.getWords("dictionaryId"),
        )
    }
    // endregion

    // region default filter
    private fun languages(from: String, to: String, at: Long = 0L) = DictionaryLanguages(from, to, at)

    @Test
    fun `the default filter combines own and imported dictionaries`() = runTest {
        coEvery { getOwnedDictionaryLanguagesUseCase() } returns listOf(languages("de", "en"), languages("tr", "en"))
        coEvery { profileService.getUserId() } returns "user-id"
        coEvery { getVaultDictionaryLanguagesApiUseCase("user-id") } returns
            listOf(languages("EN", "TR"), languages("FR", "DE"))

        assertEquals(
            ForumDictionaryFilter(
                languagePairs = listOf(
                    // Two dictionaries (one own, one imported) beat one.
                    ForumLanguagePair(SupportedLanguage.TURKISH, SupportedLanguage.ENGLISH),
                    ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.ENGLISH),
                    ForumLanguagePair(SupportedLanguage.FRENCH, SupportedLanguage.GERMAN),
                ),
            ),
            subject.getDefaultFilter(),
        )
    }

    @Test
    fun `an unreachable vault still leaves the own pairs`() = runTest {
        coEvery { getOwnedDictionaryLanguagesUseCase() } returns listOf(languages("de", "en"))
        coEvery { profileService.getUserId() } throws RuntimeException("offline")

        assertEquals(
            ForumDictionaryFilter(languagePairs = listOf(ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.ENGLISH))),
            subject.getDefaultFilter(),
        )
    }

    @Test
    fun `no dictionaries at all is an empty filter`() = runTest {
        coEvery { getOwnedDictionaryLanguagesUseCase() } returns emptyList()
        coEvery { profileService.getUserId() } returns "user-id"
        coEvery { getVaultDictionaryLanguagesApiUseCase("user-id") } returns emptyList()

        assertEquals(ForumDictionaryFilter(), subject.getDefaultFilter())
    }
    // endregion

    // region download
    @Test
    fun `a recorded import is copied into the Bibliotheca with the publisher's name`() = runTest {
        coEvery { importMarketplaceDictionaryApiUseCase("d1") } returns MarketplaceImportResult.Imported

        val result = subject.downloadDictionary(testForumDictionaryUi(dictionaryId = "d1", publisherName = "Anna"))

        assertEquals(MarketplaceImportResult.Imported, result)
        coVerify(exactly = 1) { saveDownloadedDictionaryUseCase("d1", "Anna") }
    }

    @Test
    fun `a failed local copy does not fail the download`() = runTest {
        coEvery { importMarketplaceDictionaryApiUseCase("d1") } returns MarketplaceImportResult.Imported
        coEvery { saveDownloadedDictionaryUseCase(any(), any()) } throws RuntimeException("offline")

        assertEquals(
            MarketplaceImportResult.Imported,
            subject.downloadDictionary(testForumDictionaryUi(dictionaryId = "d1")),
        )
    }

    @Test
    fun `nothing is copied when the import was refused`() = runTest {
        coEvery { importMarketplaceDictionaryApiUseCase("d1") } returns MarketplaceImportResult.Unavailable

        subject.downloadDictionary(testForumDictionaryUi(dictionaryId = "d1"))

        coVerify(exactly = 0) { saveDownloadedDictionaryUseCase(any(), any()) }
    }
    // endregion
}
