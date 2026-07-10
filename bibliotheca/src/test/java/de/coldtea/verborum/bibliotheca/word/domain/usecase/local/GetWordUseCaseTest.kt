package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.testWordEntity
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWordUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: GetWordUseCase

    override fun setUp() {
        super.setUp()
        useCase = GetWordUseCase(wordRepository)
    }

    // region invoke

    @Test
    fun `invoke returns the requested word converted to domain model`() = runTest {
        val entity = testWordEntity(wordId = "word-1")
        coEvery { wordRepository.getWord("word-1") } returns entity

        val result = useCase.invoke("word-1")

        assertEquals(entity.convertToWord(), result)
    }

    // endregion
}
