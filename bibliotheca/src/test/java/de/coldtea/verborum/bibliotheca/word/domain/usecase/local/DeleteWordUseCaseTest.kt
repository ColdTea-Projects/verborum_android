package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteWordUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: DeleteWordUseCase

    override fun setUp() {
        super.setUp()
        useCase = DeleteWordUseCase(wordRepository)
    }

    // region invoke

    @Test
    fun `invoke deletes the wordId wrapped in a single-element list`() = runTest {
        useCase.invoke("word-1")

        coVerify(exactly = 1) { wordRepository.deleteWords(listOf("word-1")) }
    }

    // endregion
}
