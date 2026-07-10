package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteWordByDictionaryIdUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: DeleteWordByDictionaryIdUseCase

    override fun setUp() {
        super.setUp()
        useCase = DeleteWordByDictionaryIdUseCase(wordRepository)
    }

    // region invoke

    @Test
    fun `invoke delegates the dictionaryId to the repository`() = runTest {
        useCase.invoke("dict-1")

        coVerify(exactly = 1) { wordRepository.deleteWordsByDictionary("dict-1") }
    }

    // endregion
}
