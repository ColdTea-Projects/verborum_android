package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.data.db.entity.DictionaryWordCount
import de.coldtea.verborum.core.BaseTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveWordCountsUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: ObserveWordCountsUseCase

    override fun setUp() {
        super.setUp()
        useCase = ObserveWordCountsUseCase(wordRepository)
    }

    @Test
    fun `invoke keys the repository counts by dictionary id`() = runTest {
        every { wordRepository.observeWordCounts() } returns flowOf(
            listOf(
                DictionaryWordCount(dictionaryId = "dict-1", count = 3),
                DictionaryWordCount(dictionaryId = "dict-2", count = 7),
            )
        )

        val result = useCase.invoke().first()

        assertEquals(mapOf("dict-1" to 3, "dict-2" to 7), result)
    }

    @Test
    fun `invoke maps an empty list to an empty map`() = runTest {
        every { wordRepository.observeWordCounts() } returns flowOf(emptyList())

        assertEquals(emptyMap<String, Int>(), useCase.invoke().first())
    }
}
