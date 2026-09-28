package de.coldtea.verborum.forum.marketplace.domain

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingsApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceWordsApiUseCase
import de.coldtea.verborum.forum.testForumDictionaryUi
import de.coldtea.verborum.forum.testForumWordUi
import de.coldtea.verborum.forum.testMarketplaceListing
import de.coldtea.verborum.forum.testMarketplaceWord
import io.mockk.coEvery
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
    fun `getDictionaries maps listings to UI models, import count as downloads`() = runTest {
        coEvery { getMarketplaceListingsApiUseCase() } returns listOf(
            testMarketplaceListing(dictionaryId = "a", importCount = 42),
            testMarketplaceListing(dictionaryId = "b", publisherName = null, rating = null),
        )

        assertEquals(
            listOf(
                testForumDictionaryUi(dictionaryId = "a", downloadCount = 42),
                testForumDictionaryUi(dictionaryId = "b", publisherName = null, rating = null),
            ),
            subject.getDictionaries(),
        )
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
