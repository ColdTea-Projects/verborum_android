package de.coldtea.verborum.forum.marketplace.domain

import androidx.paging.testing.asSnapshot
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingPage
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingsApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceWordsApiUseCase
import de.coldtea.verborum.forum.testForumDictionaryUi
import de.coldtea.verborum.forum.testForumWordUi
import de.coldtea.verborum.forum.testMarketplaceListing
import de.coldtea.verborum.forum.testMarketplaceWord
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

    private lateinit var subject: MarketplaceService

    override fun setUp() {
        super.setUp()
        subject = MarketplaceService(
            getMarketplaceListingsApiUseCase,
            getMarketplaceListingApiUseCase,
            getMarketplaceWordsApiUseCase,
        )
    }

    @Test
    fun `getDictionaries pages listings as UI models, import count as downloads`() = runTest {
        coEvery {
            getMarketplaceListingsApiUseCase(0, MarketplaceService.LISTING_PAGE_SIZE, MarketplaceListingFilter())
        } returns MarketplaceListingPage(
            listings = listOf(
                testMarketplaceListing(dictionaryId = "a", importCount = 42),
                testMarketplaceListing(dictionaryId = "b", publisherName = null, rating = null),
            ),
            page = 0,
            hasMore = false,
        )

        assertEquals(
            listOf(
                testForumDictionaryUi(dictionaryId = "a", downloadCount = 42),
                testForumDictionaryUi(dictionaryId = "b", publisherName = null, rating = null),
            ),
            subject.getDictionaries(ForumDictionaryFilter()).asSnapshot(),
        )
    }

    @Test
    fun `getDictionaries sends the filter with uppercase wire language codes`() = runTest {
        coEvery {
            getMarketplaceListingsApiUseCase(any(), any(), any())
        } returns MarketplaceListingPage(listings = emptyList(), page = 0, hasMore = false)

        subject.getDictionaries(
            ForumDictionaryFilter(
                fromLanguage = SupportedLanguage.ENGLISH,
                toLanguage = SupportedLanguage.GERMAN,
                tags = setOf("a1", "travel"),
                userName = "anna",
            )
        ).asSnapshot()

        coVerify {
            getMarketplaceListingsApiUseCase(
                0,
                MarketplaceService.LISTING_PAGE_SIZE,
                MarketplaceListingFilter(fromLang = "EN", toLang = "DE", tags = setOf("a1", "travel"), publisherName = "anna"),
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
}
