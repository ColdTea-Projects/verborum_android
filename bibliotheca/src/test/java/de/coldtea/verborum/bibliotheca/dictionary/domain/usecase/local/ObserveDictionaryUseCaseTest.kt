package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.testDictionaryEntity
import de.coldtea.verborum.core.BaseTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveDictionaryUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var useCase: ObserveDictionaryUseCase

    override fun setUp() {
        super.setUp()
        useCase = ObserveDictionaryUseCase(dictionaryRepository)
    }

    // region invoke

    @Test
    fun `invoke maps the observed entity to a domain model`() = runTest {
        val entity = testDictionaryEntity(dictionaryId = "dict-1")
        every { dictionaryRepository.observeDictionary("dict-1") } returns flowOf(entity)

        val result = useCase.invoke("dict-1").first()

        assertEquals(entity.convertToDictionary(), result)
    }

    // endregion
}
