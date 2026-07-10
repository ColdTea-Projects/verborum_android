package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CleanDictionariesUseCaseTest : BaseTest() {

    @MockK
    private lateinit var deleteDictionaryUseCase: DeleteDictionaryUseCase

    @MockK
    private lateinit var getAllDictionariesUseCase: GetAllDictionariesUseCase

    private lateinit var useCase: CleanDictionariesUseCase

    override fun setUp() {
        super.setUp()
        useCase = CleanDictionariesUseCase(
            deleteDictionaryUseCase = deleteDictionaryUseCase,
            getAllDictionariesUseCase = getAllDictionariesUseCase,
        )
    }

    // region invoke

    @Test
    fun `invoke deletes every dictionary returned by GetAllDictionariesUseCase`() = runTest {
        coEvery { getAllDictionariesUseCase.invoke() } returns listOf(
            testDictionary(dictionaryId = "dict-1"),
            testDictionary(dictionaryId = "dict-2"),
        )

        useCase.invoke()

        coVerify(exactly = 1) { deleteDictionaryUseCase.invoke("dict-1") }
        coVerify(exactly = 1) { deleteDictionaryUseCase.invoke("dict-2") }
    }

    @Test
    fun `invoke deletes nothing when there are no dictionaries`() = runTest {
        coEvery { getAllDictionariesUseCase.invoke() } returns emptyList()

        useCase.invoke()

        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
    }

    // endregion
}
