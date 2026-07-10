package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.testWordEntity
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWordsByDictionaryUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: GetWordsByDictionaryUseCase

    override fun setUp() {
        super.setUp()
        useCase = GetWordsByDictionaryUseCase(wordRepository)
    }

    // region invoke

    @Test
    fun `invoke maps repository entities to domain models preserving order`() = runTest {
        val entities = listOf(
            testWordEntity(wordId = "word-1", word = "apple"),
            testWordEntity(wordId = "word-2", word = "bread"),
        )
        coEvery { wordRepository.getWordsByDictionary("dict-1") } returns entities

        val result = useCase.invoke("dict-1")

        assertEquals(entities.map { it.convertToWord() }, result)
    }

    @Test
    fun `invoke returns empty list when the dictionary has no words`() = runTest {
        coEvery { wordRepository.getWordsByDictionary("dict-1") } returns emptyList()

        val result = useCase.invoke("dict-1")

        assertEquals(emptyList<Any>(), result)
    }

    // endregion
}
