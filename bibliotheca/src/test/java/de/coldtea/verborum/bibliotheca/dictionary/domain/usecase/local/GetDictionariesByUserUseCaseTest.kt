package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.testDictionaryEntity
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDictionariesByUserUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var useCase: GetDictionariesByUserUseCase

    override fun setUp() {
        super.setUp()
        useCase = GetDictionariesByUserUseCase(dictionaryRepository)
    }

    // region invoke

    @Test
    fun `invoke queries the repository with the given userId`() = runTest {
        coEvery { dictionaryRepository.getAllDictionariesByUser(any()) } returns emptyList()

        useCase.invoke("user-42")

        coVerify(exactly = 1) { dictionaryRepository.getAllDictionariesByUser("user-42") }
    }

    @Test
    fun `invoke maps repository entities to domain models`() = runTest {
        val entities = listOf(
            testDictionaryEntity(dictionaryId = "dict-1", userId = "user-42"),
            testDictionaryEntity(dictionaryId = "dict-2", userId = "user-42"),
        )
        coEvery { dictionaryRepository.getAllDictionariesByUser("user-42") } returns entities

        val result = useCase.invoke("user-42")

        assertEquals(entities.map { it.convertToDictionary() }, result)
    }

    // endregion
}
