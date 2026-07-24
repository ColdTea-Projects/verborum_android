package de.coldtea.verborum.bibliotheca.auth.domain.usecase

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MigrateGuestDataUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryRepository: DictionaryRepository

    @MockK
    private lateinit var wordRepository: WordRepository

    private lateinit var useCase: MigrateGuestDataUseCase

    override fun setUp() {
        super.setUp()
        useCase = MigrateGuestDataUseCase(dictionaryRepository, wordRepository)
    }

    @Test
    fun `reassigns guest dictionaries and re-flags their words unsynced`() = runTest {
        coEvery { dictionaryRepository.reassignOwner(GUEST_USER_ID, "sub-1") } returns 3

        useCase.invoke("sub-1")

        coVerify(exactly = 1) { dictionaryRepository.reassignOwner(GUEST_USER_ID, "sub-1") }
        coVerify(exactly = 1) { wordRepository.markWordsUnsyncedForUser("sub-1") }
    }

    @Test
    fun `does not touch words when there were no guest dictionaries to re-own`() = runTest {
        coEvery { dictionaryRepository.reassignOwner(GUEST_USER_ID, "sub-1") } returns 0

        useCase.invoke("sub-1")

        coVerify(exactly = 0) { wordRepository.markWordsUnsyncedForUser(any()) }
    }

    @Test
    fun `is a no-op when the subject is itself the guest id`() = runTest {
        useCase.invoke(GUEST_USER_ID)

        coVerify(exactly = 0) { dictionaryRepository.reassignOwner(any(), any()) }
        coVerify(exactly = 0) { wordRepository.markWordsUnsyncedForUser(any()) }
    }
}
