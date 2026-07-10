package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpsertWordsUseCaseTest : BaseTest() {

    @MockK(relaxed = true)
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: UpsertWordsUseCase

    override fun setUp() {
        super.setUp()
        useCase = UpsertWordsUseCase(wordRepository)
    }

    @Test
    fun `invoke saves the converted entities preserving the synced flag`() = runTest {
        val syncedWord = testWord(wordId = "word-1", isSynced = true)
        val unsyncedWord = testWord(wordId = "word-2", isSynced = false)

        useCase.invoke(listOf(syncedWord, unsyncedWord))

        coVerify(exactly = 1) {
            wordRepository.saveWords(
                listOf(syncedWord.convertToEntity(), unsyncedWord.convertToEntity())
            )
        }
    }
}
