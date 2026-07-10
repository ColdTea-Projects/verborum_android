package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteDictionaryUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: DeleteDictionaryUseCase

    override fun setUp() {
        super.setUp()
        useCase = DeleteDictionaryUseCase(
            dictionaryRepository = dictionaryRepository,
            wordRepository = wordRepository,
        )
    }

    // region invoke

    @Test
    fun `invoke deletes the words of the dictionary`() = runTest {
        useCase.invoke("dict-1")

        coVerify(exactly = 1) { wordRepository.deleteWordsByDictionary("dict-1") }
    }

    @Test
    fun `invoke deletes the dictionary itself`() = runTest {
        useCase.invoke("dict-1")

        coVerify(exactly = 1) { dictionaryRepository.deleteDictionary("dict-1") }
    }

    // endregion
}
