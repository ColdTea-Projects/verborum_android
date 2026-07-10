package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveDictionaryUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var useCase: SaveDictionaryUseCase

    override fun setUp() {
        super.setUp()
        useCase = SaveDictionaryUseCase(dictionaryRepository)
    }

    // region new dictionary (empty dictionaryId)

    @Test
    fun `invoke with empty dictionaryId saves entity with a generated non-blank id`() = runTest {
        val savedSlot = slot<DictionaryEntity>()
        coEvery { dictionaryRepository.saveDictionary(capture(savedSlot)) } returns 1L

        useCase.invoke(testDictionary(dictionaryId = ""))

        assertTrue(savedSlot.captured.dictionaryId.isNotBlank())
    }

    @Test
    fun `invoke with empty dictionaryId returns the same id it saved`() = runTest {
        val savedSlot = slot<DictionaryEntity>()
        coEvery { dictionaryRepository.saveDictionary(capture(savedSlot)) } returns 1L

        val returnedId = useCase.invoke(testDictionary(dictionaryId = ""))

        assertEquals(savedSlot.captured.dictionaryId, returnedId)
    }

    // endregion

    // region existing dictionary (non-empty dictionaryId)

    @Test
    fun `invoke with existing dictionaryId preserves the id on the saved entity`() = runTest {
        val savedSlot = slot<DictionaryEntity>()
        coEvery { dictionaryRepository.saveDictionary(capture(savedSlot)) } returns 1L

        useCase.invoke(testDictionary(dictionaryId = "dict-existing-42"))

        assertEquals("dict-existing-42", savedSlot.captured.dictionaryId)
    }

    @Test
    fun `invoke with existing dictionaryId returns the original id`() = runTest {
        coEvery { dictionaryRepository.saveDictionary(any()) } returns 1L

        val returnedId = useCase.invoke(testDictionary(dictionaryId = "dict-existing-42"))

        assertEquals("dict-existing-42", returnedId)
    }

    // endregion

    // region entity conversion

    @Test
    fun `invoke converts all dictionary fields to the saved entity`() = runTest {
        val dictionary = testDictionary(
            dictionaryId = "dict-42",
            userId = "user-7",
            name = "Spanish Food",
            isPublic = true,
            isSynced = true,
            fromLang = "es",
            toLang = "en",
            createdAt = 111L,
            updatedAt = 222L,
        )
        val savedSlot = slot<DictionaryEntity>()
        coEvery { dictionaryRepository.saveDictionary(capture(savedSlot)) } returns 1L

        useCase.invoke(dictionary)

        assertEquals(dictionary.convertToEntity(), savedSlot.captured)
    }

    // endregion
}
