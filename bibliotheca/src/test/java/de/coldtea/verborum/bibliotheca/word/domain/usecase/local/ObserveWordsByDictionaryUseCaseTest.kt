package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.testWordEntity
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveWordsByDictionaryUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: ObserveWordsByDictionaryUseCase

    override fun setUp() {
        super.setUp()
        useCase = ObserveWordsByDictionaryUseCase(wordRepository)
    }

    // region invoke

    @Test
    fun `invoke maps entity list emissions to domain models`() = runTest {
        val entities = listOf(
            testWordEntity(wordId = "word-1"),
            testWordEntity(wordId = "word-2"),
        )
        every { wordRepository.observeWordsByDictionary("dict-1") } returns flow { emit(entities) }

        val result = useCase.invoke("dict-1").first()

        assertEquals(entities.map { it.convertToWord() }, result)
    }

    @Test
    fun `invoke passes every upstream emission through`() = runTest {
        val first = listOf(testWordEntity(wordId = "word-1"))
        val second = listOf(
            testWordEntity(wordId = "word-1"),
            testWordEntity(wordId = "word-2"),
        )
        every { wordRepository.observeWordsByDictionary("dict-1") } returns flow {
            emit(first)
            emit(second)
        }

        val emissions = useCase.invoke("dict-1").toList()

        assertEquals(2, emissions.size)
        assertEquals(1, emissions[0].size)
        assertEquals(2, emissions[1].size)
    }

    // endregion
}
