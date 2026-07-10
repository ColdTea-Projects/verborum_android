package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.testDictionary
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
    fun `observeDictionaries emits both events when list content changes`() = runTest {
        val first = listOf(testDictionary(dictionaryId = "dict-1", name = "Old"))
        val second = listOf(testDictionary(dictionaryId = "dict-1", name = "New"))
        every { observeAllDictionariesUseCase.invoke() } returns flow {
            emit(first)
            emit(second)
        }

        val emissions = dictionaryService.observeDictionaries().toList()

        assertEquals(2, emissions.size)
        assertEquals("Old", emissions[0].first().name)
        assertEquals("New", emissions[1].first().name)
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

    // region deleteDictionary

    @Test
    fun `deleteDictionary delegates to uploadService`() = runTest {
        coEvery { uploadService.deleteDictionary("dict-1") } returns mockk()

        dictionaryService.deleteDictionary("dict-1")

        coVerify(exactly = 1) { uploadService.deleteDictionary("dict-1") }
    }

    // endregion
}
