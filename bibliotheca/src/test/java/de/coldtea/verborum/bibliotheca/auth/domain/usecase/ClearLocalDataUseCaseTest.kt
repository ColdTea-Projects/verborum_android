package de.coldtea.verborum.bibliotheca.auth.domain.usecase

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClearLocalDataUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: ClearLocalDataUseCase

    override fun setUp() {
        super.setUp()
        useCase = ClearLocalDataUseCase(dictionaryRepository, wordRepository)
    }

    @Test
    fun `invoke deletes every local word and dictionary`() = runTest {
        useCase.invoke()

        coVerify(exactly = 1) { wordRepository.deleteAllWords() }
        coVerify(exactly = 1) { dictionaryRepository.deleteAllDictionaries() }
    }
}
