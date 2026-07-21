package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservePendingUploadsUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: ObservePendingUploadsUseCase

    override fun setUp() {
        super.setUp()
        useCase = ObservePendingUploadsUseCase(dictionaryRepository, wordRepository)
    }

    @Test
    fun `invoke sums the pending dictionary and word counts`() = runTest {
        every { dictionaryRepository.observePendingUploadCount() } returns flowOf(2)
        every { wordRepository.observePendingUploadCount() } returns flowOf(5)

        assertEquals(7, useCase.invoke().first())
    }

    @Test
    fun `invoke is zero when nothing is pending on either side`() = runTest {
        every { dictionaryRepository.observePendingUploadCount() } returns flowOf(0)
        every { wordRepository.observePendingUploadCount() } returns flowOf(0)

        assertEquals(0, useCase.invoke().first())
    }
}
