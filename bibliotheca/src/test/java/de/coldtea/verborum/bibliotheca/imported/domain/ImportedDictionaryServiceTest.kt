package de.coldtea.verborum.bibliotheca.imported.domain

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionaryContent
import de.coldtea.verborum.bibliotheca.imported.domain.model.VaultEntry
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.FetchImportedDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.GetImportedDictionaryRatingApiUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.GetVaultApiUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.RemoveImportedDictionaryRatingApiUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.SetImportedDictionaryRatingApiUseCase
import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.RemoveFromVaultApiUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.DeleteImportedDictionaryUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.GetImportedDictionariesUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedDictionariesUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedDictionaryIdsUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedDictionaryUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedWordCountsUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedWordsInLanguagePairUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedWordsUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.SaveImportedDictionaryUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.UpdateImportedWordLevelUseCase
import de.coldtea.verborum.bibliotheca.testImportedDictionary
import de.coldtea.verborum.bibliotheca.testImportedWord
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.profile.domain.ProfileService
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileUi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportedDictionaryServiceTest : BaseTest() {

    @MockK private lateinit var observeImportedDictionariesUseCase: ObserveImportedDictionariesUseCase
    @MockK private lateinit var observeImportedDictionaryUseCase: ObserveImportedDictionaryUseCase
    @MockK private lateinit var observeImportedDictionaryIdsUseCase: ObserveImportedDictionaryIdsUseCase
    @MockK private lateinit var observeImportedWordsUseCase: ObserveImportedWordsUseCase
    @MockK private lateinit var observeImportedWordsInLanguagePairUseCase: ObserveImportedWordsInLanguagePairUseCase
    @MockK private lateinit var observeImportedWordCountsUseCase: ObserveImportedWordCountsUseCase
    @MockK(relaxed = true) private lateinit var updateImportedWordLevelUseCase: UpdateImportedWordLevelUseCase
    @MockK private lateinit var getImportedDictionariesUseCase: GetImportedDictionariesUseCase
    @MockK(relaxed = true) private lateinit var saveImportedDictionaryUseCase: SaveImportedDictionaryUseCase
    @MockK(relaxed = true) private lateinit var deleteImportedDictionaryUseCase: DeleteImportedDictionaryUseCase
    @MockK private lateinit var fetchImportedDictionaryApiUseCase: FetchImportedDictionaryApiUseCase
    @MockK private lateinit var getVaultApiUseCase: GetVaultApiUseCase
    @MockK private lateinit var removeFromVaultApiUseCase: RemoveFromVaultApiUseCase
    @MockK private lateinit var profileService: ProfileService
    @MockK private lateinit var getImportedDictionaryRatingApiUseCase: GetImportedDictionaryRatingApiUseCase
    @MockK private lateinit var setImportedDictionaryRatingApiUseCase: SetImportedDictionaryRatingApiUseCase
    @MockK private lateinit var removeImportedDictionaryRatingApiUseCase: RemoveImportedDictionaryRatingApiUseCase

    private lateinit var service: ImportedDictionaryService

    override fun setUp() {
        super.setUp()
        coEvery { profileService.getUserId() } returns USER_ID
        every { profileService.observeProfile() } returns flowOf(profile(isMember = true))
        service = ImportedDictionaryService(
            observeImportedDictionariesUseCase,
            observeImportedDictionaryUseCase,
            observeImportedDictionaryIdsUseCase,
            observeImportedWordsUseCase,
            observeImportedWordsInLanguagePairUseCase,
            observeImportedWordCountsUseCase,
            updateImportedWordLevelUseCase,
            getImportedDictionariesUseCase,
            saveImportedDictionaryUseCase,
            deleteImportedDictionaryUseCase,
            fetchImportedDictionaryApiUseCase,
            getVaultApiUseCase,
            removeFromVaultApiUseCase,
            profileService,
            getImportedDictionaryRatingApiUseCase,
            setImportedDictionaryRatingApiUseCase,
            removeImportedDictionaryRatingApiUseCase,
        )
    }

    private fun profile(isMember: Boolean) =
        ProfileUi(email = "a@b.c", displayName = "Anna", isMarketplaceMember = isMember)

    private fun content(id: String, publisherName: String? = "Anna Bauer", importedAt: Long = 1_000L) =
        ImportedDictionaryContent(
            dictionary = testImportedDictionary(dictionaryId = id, publisherName = publisherName, importedAt = importedAt),
            words = listOf(testImportedWord(dictionaryId = id)),
        )

    // region observe
    @Test
    fun `observeDictionaries marks every download as imported, with its publisher`() = runTest {
        every { observeImportedDictionariesUseCase.invoke() } returns flowOf(listOf(testImportedDictionary()))

        val result = service.observeDictionaries().first().single()

        assertTrue(result.isImported)
        assertEquals("Anna Bauer", result.publisherName)
        assertEquals("publisher-sub", result.userId)
    }

    @Test
    fun `downloads are hidden everywhere once the user is known not to be a member`() = runTest {
        every { profileService.observeProfile() } returns flowOf(profile(isMember = false))
        every { observeImportedDictionariesUseCase.invoke() } returns flowOf(listOf(testImportedDictionary()))
        every { observeImportedDictionaryUseCase.invoke("d1") } returns flowOf(testImportedDictionary())
        every { observeImportedWordsUseCase.invoke("d1") } returns flowOf(listOf(testImportedWord()))
        every { observeImportedWordsInLanguagePairUseCase.invoke("de", "en") } returns flowOf(listOf(testImportedWord()))
        every { observeImportedWordCountsUseCase.invoke() } returns flowOf(mapOf("d1" to 1))

        assertTrue(service.observeDictionaries().first().isEmpty())
        assertEquals(null, service.observeDictionary("d1").first())
        assertTrue(service.observeWords("d1").first().isEmpty())
        assertTrue(service.observeWordsInLanguagePair("de", "en").first().isEmpty())
        assertTrue(service.observeWordCounts().first().isEmpty())
    }

    @Test
    fun `downloads stay visible while the profile is not loaded yet`() = runTest {
        every { profileService.observeProfile() } returns flowOf(null)
        every { observeImportedDictionariesUseCase.invoke() } returns flowOf(listOf(testImportedDictionary()))

        assertEquals(1, service.observeDictionaries().first().size)
    }
    // endregion

    // region download
    @Test
    fun `addImportedDictionary stores the owner's current version with the listing's publisher name`() = runTest {
        coEvery { fetchImportedDictionaryApiUseCase.invoke("d1", "Anna Bauer", any()) } returns content("d1")

        service.addImportedDictionary("d1", "Anna Bauer")

        coVerify { saveImportedDictionaryUseCase.invoke(content("d1")) }
    }

    @Test
    fun `addImportedDictionary stores nothing for an unreadable dictionary`() = runTest {
        coEvery { fetchImportedDictionaryApiUseCase.invoke(any(), any(), any()) } returns null

        service.addImportedDictionary("gone", null)

        coVerify(exactly = 0) { saveImportedDictionaryUseCase.invoke(any()) }
    }
    // endregion

    // region vault refresh
    @Test
    fun `refresh re-reads every vault entry, keeping the known publisher name and import time`() = runTest {
        coEvery { getVaultApiUseCase.invoke(USER_ID) } returns listOf(VaultEntry("d1", importedAt = 9_000L))
        coEvery { getImportedDictionariesUseCase.invoke() } returns
            listOf(testImportedDictionary(dictionaryId = "d1", publisherName = "Anna Bauer", importedAt = 1_000L))
        coEvery { fetchImportedDictionaryApiUseCase.invoke("d1", "Anna Bauer", 1_000L) } returns content("d1")

        service.refreshFromVault()

        coVerify { saveImportedDictionaryUseCase.invoke(content("d1")) }
    }

    @Test
    fun `refresh adds a dictionary downloaded on another device`() = runTest {
        coEvery { getVaultApiUseCase.invoke(USER_ID) } returns listOf(VaultEntry("d2", importedAt = 5_000L))
        coEvery { getImportedDictionariesUseCase.invoke() } returns emptyList()
        coEvery { fetchImportedDictionaryApiUseCase.invoke("d2", null, 5_000L) } returns content("d2", publisherName = null)

        service.refreshFromVault()

        coVerify { saveImportedDictionaryUseCase.invoke(content("d2", publisherName = null)) }
    }

    @Test
    fun `refresh drops a dictionary that is no longer readable`() = runTest {
        coEvery { getVaultApiUseCase.invoke(USER_ID) } returns listOf(VaultEntry("gone", importedAt = null))
        coEvery { getImportedDictionariesUseCase.invoke() } returns listOf(testImportedDictionary(dictionaryId = "gone"))
        coEvery { fetchImportedDictionaryApiUseCase.invoke("gone", any(), any()) } returns null

        service.refreshFromVault()

        coVerify { deleteImportedDictionaryUseCase.invoke("gone") }
    }

    @Test
    fun `refresh removes one unsubscribed elsewhere, but not one downloaded moments ago`() = runTest {
        coEvery { getVaultApiUseCase.invoke(USER_ID) } returns emptyList()
        coEvery { getImportedDictionariesUseCase.invoke() } returns listOf(
            testImportedDictionary(dictionaryId = "old", importedAt = 0L),
            testImportedDictionary(dictionaryId = "fresh", importedAt = getNowInMillis()),
        )

        service.refreshFromVault()

        coVerify(exactly = 1) { deleteImportedDictionaryUseCase.invoke("old") }
        coVerify(exactly = 0) { deleteImportedDictionaryUseCase.invoke("fresh") }
    }

    @Test
    fun `a network failure on one dictionary keeps its local copy and refreshes the rest`() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        coEvery { getVaultApiUseCase.invoke(USER_ID) } returns listOf(VaultEntry("a", 1L), VaultEntry("b", 1L))
        coEvery { getImportedDictionariesUseCase.invoke() } returns emptyList()
        coEvery { fetchImportedDictionaryApiUseCase.invoke("a", any(), any()) } throws RuntimeException("offline")
        coEvery { fetchImportedDictionaryApiUseCase.invoke("b", any(), any()) } returns content("b")

        service.refreshFromVault()

        coVerify(exactly = 0) { deleteImportedDictionaryUseCase.invoke("a") }
        coVerify { saveImportedDictionaryUseCase.invoke(content("b")) }
    }
    // endregion

    // region unsubscribe
    @Test
    fun `unsubscribe removes the vault entry before the local copy`() = runTest {
        coEvery { removeFromVaultApiUseCase.invoke(USER_ID, "d1") } returns Unit

        service.unsubscribe("d1")

        coVerifyOrder {
            removeFromVaultApiUseCase.invoke(USER_ID, "d1")
            deleteImportedDictionaryUseCase.invoke("d1")
        }
    }

    @Test(expected = RuntimeException::class)
    fun `unsubscribe keeps the local copy when the server cannot be reached`() = runTest {
        coEvery { removeFromVaultApiUseCase.invoke(any(), any()) } throws RuntimeException("offline")

        try {
            service.unsubscribe("d1")
        } finally {
            coVerify(exactly = 0) { deleteImportedDictionaryUseCase.invoke(any()) }
        }
    }
    // endregion

    // region rating
    @Test
    fun `rating calls go to the marketplace use cases`() = runTest {
        coEvery { getImportedDictionaryRatingApiUseCase.invoke("d1") } returns 3
        coEvery { setImportedDictionaryRatingApiUseCase.invoke("d1", 5) } returns RatingOutcome.DONE
        coEvery { removeImportedDictionaryRatingApiUseCase.invoke("d1") } returns RatingOutcome.UNAVAILABLE

        assertEquals(3, service.getMyRating("d1"))
        assertEquals(RatingOutcome.DONE, service.setRating("d1", 5))
        assertEquals(RatingOutcome.UNAVAILABLE, service.removeRating("d1"))
    }
    // endregion

    private companion object {
        const val USER_ID = "4140c0de-0000-4000-8000-0000000000bb"
    }
}
