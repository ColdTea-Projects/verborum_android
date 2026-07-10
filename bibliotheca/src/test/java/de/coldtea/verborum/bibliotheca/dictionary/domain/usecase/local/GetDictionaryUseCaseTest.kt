package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.testDictionaryEntity
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDictionaryUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var useCase: GetDictionaryUseCase

    override fun setUp() {
        super.setUp()
        useCase = GetDictionaryUseCase(dictionaryRepository)
    }

    // region invoke

    @Test
    fun `invoke returns the requested dictionary converted to domain model`() = runTest {
        val entity = testDictionaryEntity(dictionaryId = "dict-1")
        coEvery { dictionaryRepository.getDictionary("dict-1") } returns entity

        val result = useCase.invoke("dict-1")

        assertEquals(entity.convertToDictionary(), result)
    }

    // endregion
}
