package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.core.auth.domain.usecase.GetActiveUserUseCase
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.DictionaryDeletion
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.DeleteDictionaryRemotelyUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.MarkDictionaryDeletedUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.bibliotheca.testDictionaryUi
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import de.coldtea.verborum.bibliotheca.imported.domain.ImportedDictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.UpdateDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.UpdateDictionarySharingUseCase
import de.coldtea.verborum.core.profile.domain.ProfileService
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileUi
import kotlinx.coroutines.flow.MutableStateFlow
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.SharingChange
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.SharingState
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryServiceTest : BaseTest() {

    @MockK
    private lateinit var observeAllDictionariesUseCase: ObserveAllDictionariesUseCase

    @MockK
    private lateinit var observeDictionaryUseCase: ObserveDictionaryUseCase

    // invoke returns Dictionary — not covered by relaxUnitFun, stubbed per test with coEvery.
    @MockK
    private lateinit var getDictionaryUseCase: GetDictionaryUseCase

    // invoke returns String — not covered by relaxUnitFun, stubbed per test with coEvery.
    @MockK
    private lateinit var saveDictionaryUseCase: SaveDictionaryUseCase

    // invoke returns the repository result (non-Unit) — relax fully for verify-only use.
    @MockK(relaxed = true)
    private lateinit var markDictionaryDeletedUseCase: MarkDictionaryDeletedUseCase

    // invoke returns the deletion outcome — stubbed per test with coEvery.
    @MockK
    private lateinit var deleteDictionaryRemotelyUseCase: DeleteDictionaryRemotelyUseCase

    // invoke returns String? — defaults to null (logged out) so createDictionary falls back to guest.
    @MockK
    private lateinit var getActiveUserUseCase: GetActiveUserUseCase

    @MockK
    private lateinit var importedDictionaryService: ImportedDictionaryService

    @MockK
    private lateinit var updateDictionaryApiUseCase: UpdateDictionaryApiUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var updateDictionarySharingUseCase: UpdateDictionarySharingUseCase

    @MockK
    private lateinit var profileService: ProfileService

    private val profile = MutableStateFlow<ProfileUi?>(null)

    private lateinit var dictionaryService: DictionaryService

    override fun setUp() {
        super.setUp()
        every { getActiveUserUseCase.invoke() } returns null
        // No downloads unless a test says otherwise.
        every { importedDictionaryService.observeDictionaries() } returns flowOf(emptyList())
        every { importedDictionaryService.observeDictionary(any()) } returns flowOf(null)
        every { profileService.observeProfile() } returns profile
        // No own dictionaries unless a test says otherwise (createDictionary reads the sharing state).
        every { observeAllDictionariesUseCase.invoke() } returns flowOf(emptyList())
        dictionaryService = DictionaryService(
            observeAllDictionariesUseCase = observeAllDictionariesUseCase,
            observeDictionaryUseCase = observeDictionaryUseCase,
            getDictionaryUseCase = getDictionaryUseCase,
            saveDictionaryUseCase = saveDictionaryUseCase,
            markDictionaryDeletedUseCase = markDictionaryDeletedUseCase,
            deleteDictionaryRemotelyUseCase = deleteDictionaryRemotelyUseCase,
            getActiveUserUseCase = getActiveUserUseCase,
            importedDictionaryService = importedDictionaryService,
            updateDictionaryApiUseCase = updateDictionaryApiUseCase,
            updateDictionarySharingUseCase = updateDictionarySharingUseCase,
            profileService = profileService,
        )
    }

    // region createDictionary

    @Test
    fun `createDictionary delegates a new guest dictionary to SaveDictionaryUseCase`() = runTest {
        val dictionarySlot = slot<Dictionary>()
        coEvery { saveDictionaryUseCase.invoke(capture(dictionarySlot)) } returns "new-dict-id"

        dictionaryService.createDictionary(name = "German Basics", fromLang = "de", toLang = "en")

        val saved = dictionarySlot.captured
        assertEquals("", saved.dictionaryId)
        assertEquals(GUEST_USER_ID, saved.userId)
        assertEquals("German Basics", saved.name)
        assertEquals("de", saved.fromLang)
        assertEquals("en", saved.toLang)
        assertFalse(saved.isPublic)
        assertFalse(saved.isSynced)
    }

    @Test
    fun `createDictionary stamps the signed-in subject as owner when logged in`() = runTest {
        every { getActiveUserUseCase.invoke() } returns "subject-123"
        val dictionarySlot = slot<Dictionary>()
        coEvery { saveDictionaryUseCase.invoke(capture(dictionarySlot)) } returns "new-dict-id"

        dictionaryService.createDictionary(name = "German Basics", fromLang = "de", toLang = "en")

        assertEquals("subject-123", dictionarySlot.captured.userId)
    }

    @Test
    fun `createDictionary returns the id produced by SaveDictionaryUseCase`() = runTest {
        coEvery { saveDictionaryUseCase.invoke(any()) } returns "generated-uuid"

        val result = dictionaryService.createDictionary(
            name = "German Basics",
            fromLang = "de",
            toLang = "en",
        )

        assertEquals("generated-uuid", result)
    }

    // endregion

    // region downloads
    @Test
    fun `observeDictionaries lists downloads after the own dictionaries`() = runTest {
        val own = testDictionary(dictionaryId = "own")
        val downloaded = testDictionaryUi(dictionaryId = "dl").copy(isImported = true, publisherName = "Anna")
        every { observeAllDictionariesUseCase.invoke() } returns flowOf(listOf(own))
        every { importedDictionaryService.observeDictionaries() } returns flowOf(listOf(downloaded))

        val result = dictionaryService.observeDictionaries().first()

        assertEquals(listOf("own", "dl"), result.map { it.dictionaryId })
        assertEquals(listOf(false, true), result.map { it.isImported })
    }

    @Test
    fun `observeDictionary falls back to a downloaded dictionary`() = runTest {
        val downloaded = testDictionaryUi(dictionaryId = "dl").copy(isImported = true)
        every { observeDictionaryUseCase.invoke("dl") } returns flowOf(null)
        every { importedDictionaryService.observeDictionary("dl") } returns flowOf(downloaded)

        assertEquals(downloaded, dictionaryService.observeDictionary("dl").first())
    }

    @Test
    fun `unsubscribe delegates to the downloads`() = runTest {
        dictionaryService.unsubscribe("dl")

        coVerify(exactly = 1) { importedDictionaryService.unsubscribe("dl") }
    }
    // endregion

    // region observeDictionaries

    @Test
    fun `observeDictionaries maps domain Dictionary to DictionaryUi`() = runTest {
        val dictionaries = listOf(
            testDictionary(dictionaryId = "dict-1", name = "First"),
            testDictionary(dictionaryId = "dict-2", name = "Second"),
        )
        every { observeAllDictionariesUseCase.invoke() } returns flowOf(dictionaries)

        val result = dictionaryService.observeDictionaries().first()

        assertEquals(dictionaries.map(Dictionary::convertToUi), result)
    }

    @Test
    fun `observeDictionaries suppresses duplicate emissions`() = runTest {
        val dictionaries = listOf(testDictionary(dictionaryId = "dict-1"))
        // emit the exact same list twice — only one downstream emission expected
        every { observeAllDictionariesUseCase.invoke() } returns flow {
            emit(dictionaries)
            emit(dictionaries)
        }

        val emissions = dictionaryService.observeDictionaries().toList()

        assertEquals(1, emissions.size)
    }

    @Test
    fun `observeDictionaries emits the changed content`() = runTest {
        val first = listOf(testDictionary(dictionaryId = "dict-1", name = "Old"))
        val second = listOf(testDictionary(dictionaryId = "dict-1", name = "New"))
        every { observeAllDictionariesUseCase.invoke() } returns flow {
            emit(first)
            emit(second)
        }

        val emissions = dictionaryService.observeDictionaries().toList()

        // Merged with the downloads through combine, which may conflate a back-to-back
        // intermediate value — the list only ever needs the latest state.
        assertEquals("New", emissions.last().first().name)
        assertEquals(emissions.distinct(), emissions)
    }

    // endregion

    // region observeDictionary

    @Test
    fun `observeDictionary maps the observed dictionary to DictionaryUi`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1")
        every { observeDictionaryUseCase.invoke("dict-1") } returns flowOf(dictionary)

        val result = dictionaryService.observeDictionary("dict-1").first()

        assertEquals(dictionary.convertToUi(), result)
    }

    // endregion

    // region getDictionary / updateDictionary

    @Test
    fun `getDictionary maps the loaded dictionary to DictionaryUi`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1", name = "German Basics")
        coEvery { getDictionaryUseCase.invoke("dict-1") } returns dictionary

        val result = dictionaryService.getDictionary("dict-1")

        assertEquals(dictionary.convertToUi(), result)
    }

    @Test
    fun `updateDictionary preserves identity, re-marks unsynced, and delegates to SaveDictionaryUseCase`() = runTest {
        val dictionarySlot = slot<Dictionary>()
        coEvery { saveDictionaryUseCase.invoke(capture(dictionarySlot)) } returns "dict-1"

        dictionaryService.updateDictionary(
            testDictionaryUi(
                dictionaryId = "dict-1",
                name = "German Advanced",
                fromLang = "de",
                toLang = "es",
            )
        )

        val saved = dictionarySlot.captured
        assertEquals("dict-1", saved.dictionaryId)
        assertEquals("German Advanced", saved.name)
        assertEquals("de", saved.fromLang)
        assertEquals("es", saved.toLang)
        assertFalse(saved.isSynced)
    }

    // endregion

    // region sharing
    private fun member() = ProfileUi(email = "a@b.c", displayName = "Anna", isMarketplaceMember = true)

    @Test
    fun `a member's first dictionary is created shared`() = runTest {
        profile.value = member()
        val saved = slot<Dictionary>()
        coEvery { saveDictionaryUseCase.invoke(capture(saved)) } returns "new"

        dictionaryService.createDictionary(name = "First", fromLang = "de", toLang = "en")

        assertTrue(saved.captured.isPublic)
    }

    @Test
    fun `a member who already shares one creates the next private`() = runTest {
        profile.value = member()
        every { observeAllDictionariesUseCase.invoke() } returns flowOf(listOf(testDictionary(dictionaryId = "s", isPublic = true)))
        val saved = slot<Dictionary>()
        coEvery { saveDictionaryUseCase.invoke(capture(saved)) } returns "new"

        dictionaryService.createDictionary(name = "Second", fromLang = "de", toLang = "en")

        assertFalse(saved.captured.isPublic)
    }

    @Test
    fun `the sharing state names the member's shared dictionaries`() = runTest {
        profile.value = member()
        every { observeAllDictionariesUseCase.invoke() } returns flowOf(
            listOf(testDictionary(dictionaryId = "s", isPublic = true), testDictionary(dictionaryId = "p", isPublic = false))
        )

        val state = dictionaryService.observeSharingState().first()

        assertEquals(SharingState(isMember = true, sharedDictionaryIds = setOf("s")), state)
        assertTrue(state.isLastShared("s"))
    }

    @Test
    fun `setShared updates the local row only once the server agrees`() = runTest {
        coEvery { getDictionaryUseCase.invoke("d1") } returns testDictionary(dictionaryId = "d1", isPublic = true)
        coEvery { updateDictionaryApiUseCase.invoke(any()) } returns Response.success(Unit)

        assertEquals(SharingChange.UPDATED, dictionaryService.setShared("d1", isPublic = false))
        coVerify { updateDictionaryApiUseCase.invoke(match { !it.isPublic && it.dictionaryId == "d1" }) }
        coVerify(exactly = 1) { updateDictionarySharingUseCase.invoke("d1", false) }
    }

    @Test
    fun `setShared reports the keep-one-shared refusal and changes nothing`() = runTest {
        coEvery { getDictionaryUseCase.invoke("d1") } returns testDictionary(dictionaryId = "d1", isPublic = true)
        coEvery { updateDictionaryApiUseCase.invoke(any()) } returns Response.error(
            400,
            "{\"error\":\"SharingRequiredException\"}".toResponseBody("application/json".toMediaType()),
        )

        assertEquals(SharingChange.LAST_SHARED, dictionaryService.setShared("d1", isPublic = false))
        coVerify(exactly = 0) { updateDictionarySharingUseCase.invoke(any(), any()) }
    }

    @Test
    fun `setShared offline fails without touching the local row`() = runTest {
        coEvery { getDictionaryUseCase.invoke("d1") } returns testDictionary(dictionaryId = "d1")
        coEvery { updateDictionaryApiUseCase.invoke(any()) } throws IOException("offline")

        assertEquals(SharingChange.FAILED, dictionaryService.setShared("d1", isPublic = true))
        coVerify(exactly = 0) { updateDictionarySharingUseCase.invoke(any(), any()) }
    }
    // endregion

    // region deleteDictionary

    @Test
    fun `a delete the server confirms needs no tombstone`() = runTest {
        coEvery { deleteDictionaryRemotelyUseCase.invoke("dict-1") } returns DictionaryDeletion.DELETED

        assertEquals(DictionaryDeletion.DELETED, dictionaryService.deleteDictionary("dict-1"))
        coVerify(exactly = 0) { markDictionaryDeletedUseCase.invoke(any()) }
    }

    @Test
    fun `a delete the server could not get is tombstoned for sync to retry`() = runTest {
        coEvery { deleteDictionaryRemotelyUseCase.invoke("dict-1") } returns DictionaryDeletion.PENDING

        assertEquals(DictionaryDeletion.PENDING, dictionaryService.deleteDictionary("dict-1"))
        coVerify(exactly = 1) { markDictionaryDeletedUseCase.invoke("dict-1") }
    }

    @Test
    fun `a refused delete leaves the dictionary in place`() = runTest {
        coEvery { deleteDictionaryRemotelyUseCase.invoke("dict-1") } returns DictionaryDeletion.SHARING_REQUIRED

        assertEquals(DictionaryDeletion.SHARING_REQUIRED, dictionaryService.deleteDictionary("dict-1"))
        coVerify(exactly = 0) { markDictionaryDeletedUseCase.invoke(any()) }
    }

    // endregion
}
