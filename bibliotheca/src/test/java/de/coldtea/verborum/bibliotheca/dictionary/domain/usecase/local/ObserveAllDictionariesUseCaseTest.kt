package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.testDictionaryEntity
import de.coldtea.verborum.core.BaseTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveAllDictionariesUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var useCase: ObserveAllDictionariesUseCase

    override fun setUp() {
        super.setUp()
        useCase = ObserveAllDictionariesUseCase(dictionaryRepository)
    }

    // region invoke

    @Test
    fun `invoke maps entity list emissions to domain models`() = runTest {
        val entities = listOf(
            testDictionaryEntity(dictionaryId = "dict-1"),
            testDictionaryEntity(dictionaryId = "dict-2"),
        )
        every { dictionaryRepository.observeAllDictionaries() } returns flow { emit(entities) }

        val result = useCase.invoke().first()

        assertEquals(entities.map { it.convertToDictionary() }, result)
    }

    @Test
    fun `invoke passes every upstream emission through`() = runTest {
        val first = listOf(testDictionaryEntity(dictionaryId = "dict-1"))
        val second = listOf(
            testDictionaryEntity(dictionaryId = "dict-1"),
            testDictionaryEntity(dictionaryId = "dict-2"),
        )
        every { dictionaryRepository.observeAllDictionaries() } returns flow {
            emit(first)
            emit(second)
        }

        val emissions = useCase.invoke().toList()

        assertEquals(2, emissions.size)
        assertEquals(1, emissions[0].size)
        assertEquals(2, emissions[1].size)
    }

    // endregion
}
