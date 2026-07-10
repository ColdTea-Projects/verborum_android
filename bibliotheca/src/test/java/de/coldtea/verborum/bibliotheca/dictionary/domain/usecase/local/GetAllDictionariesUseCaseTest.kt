package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.testDictionaryEntity
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllDictionariesUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var useCase: GetAllDictionariesUseCase

    override fun setUp() {
        super.setUp()
        useCase = GetAllDictionariesUseCase(dictionaryRepository)
    }

    // region invoke

    @Test
    fun `invoke maps repository entities to domain models preserving order`() = runTest {
        val entities = listOf(
            testDictionaryEntity(dictionaryId = "dict-1", name = "First"),
            testDictionaryEntity(dictionaryId = "dict-2", name = "Second"),
        )
        coEvery { dictionaryRepository.getAllDictionaries() } returns entities

        val result = useCase.invoke()

        assertEquals(entities.map { it.convertToDictionary() }, result)
    }

    @Test
    fun `invoke returns empty list when repository has no dictionaries`() = runTest {
        coEvery { dictionaryRepository.getAllDictionaries() } returns emptyList()

        val result = useCase.invoke()

        assertEquals(emptyList<Any>(), result)
    }

    // endregion
}
