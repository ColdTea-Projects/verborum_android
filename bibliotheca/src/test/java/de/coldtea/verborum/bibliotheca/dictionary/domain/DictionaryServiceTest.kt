package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DictionaryServiceTest : BaseTest() {

    @MockK
    private lateinit var observeAllDictionariesUseCase: ObserveAllDictionariesUseCase

    @MockK
    private lateinit var observeDictionaryUseCase: ObserveDictionaryUseCase

    // invoke returns String — not covered by relaxUnitFun, stubbed per test with coEvery.
    @MockK
    private lateinit var saveDictionaryUseCase: SaveDictionaryUseCase

    @MockK
    private lateinit var syncService: SyncService

    @MockK
    private lateinit var uploadService: UploadService

    private lateinit var dictionaryService: DictionaryService

    override fun setUp() {
        super.setUp()
        dictionaryService = DictionaryService(
            observeAllDictionariesUseCase = observeAllDictionariesUseCase,
            observeDictionaryUseCase = observeDictionaryUseCase,
            saveDictionaryUseCase = saveDictionaryUseCase,
            syncService = syncService,
            uploadService = uploadService,
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
    fun `createDictionary returns the id produced by SaveDictionaryUseCase`() = runTest {
        coEvery { saveDictionaryUseCase.invoke(any()) } returns "generated-uuid"

        val result = dictionaryService.createDictionary(
            name = "German Basics",
            fromLang = "de",
            toLang = "en",
        )

        assertEquals("generated-uuid", result)
    }

    @Test
    fun `createDictionary does not upload or sync`() = runTest {
        coEvery { saveDictionaryUseCase.invoke(any()) } returns "new-dict-id"

        dictionaryService.createDictionary(name = "German Basics", fromLang = "de", toLang = "en")

        coVerify(exactly = 0) { uploadService.createDictionary(any()) }
        coVerify(exactly = 0) { syncService.syncDictionaries() }
    }

    // endregion
}
