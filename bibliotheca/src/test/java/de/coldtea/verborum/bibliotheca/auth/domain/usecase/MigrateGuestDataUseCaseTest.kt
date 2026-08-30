package de.coldtea.verborum.bibliotheca.auth.domain.usecase

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.testDictionaryEntity
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
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
    fun `reassigns guest dictionaries and re-flags only their words unsynced`() = runTest {
        coEvery { dictionaryRepository.getAllDictionariesByUser(GUEST_USER_ID) } returns listOf(
            testDictionaryEntity(dictionaryId = "guest-1", userId = GUEST_USER_ID),
            testDictionaryEntity(dictionaryId = "guest-2", userId = GUEST_USER_ID),
        )
        coEvery { dictionaryRepository.reassignOwner(GUEST_USER_ID, "sub-1") } returns 2

        useCase.invoke("sub-1")

        coVerify(exactly = 1) { dictionaryRepository.reassignOwner(GUEST_USER_ID, "sub-1") }
        // Only the migrated ids — never the subject's pre-existing, already-synced dictionaries.
        coVerify(exactly = 1) {
            wordRepository.markWordsUnsyncedInDictionaries(listOf("guest-1", "guest-2"))
        }
    }

    @Test
    fun `reads the guest dictionaries before re-owning them`() = runTest {
        coEvery { dictionaryRepository.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryEntity(dictionaryId = "guest-1", userId = GUEST_USER_ID))
        coEvery { dictionaryRepository.reassignOwner(GUEST_USER_ID, "sub-1") } returns 1

        useCase.invoke("sub-1")

        // Order matters: after reassignOwner the rows are owned by the subject and the guest query
        // would return nothing, so the ids have to be captured first.
        coVerifyOrder {
            dictionaryRepository.getAllDictionariesByUser(GUEST_USER_ID)
            dictionaryRepository.reassignOwner(GUEST_USER_ID, "sub-1")
            wordRepository.markWordsUnsyncedInDictionaries(listOf("guest-1"))
        }
    }

    @Test
    fun `does not touch words when there were no guest dictionaries to re-own`() = runTest {
        coEvery { dictionaryRepository.getAllDictionariesByUser(GUEST_USER_ID) } returns emptyList()

        useCase.invoke("sub-1")

        coVerify(exactly = 0) { dictionaryRepository.reassignOwner(any(), any()) }
        coVerify(exactly = 0) { wordRepository.markWordsUnsyncedInDictionaries(any()) }
    }

    @Test
    fun `is a no-op when the subject is itself the guest id`() = runTest {
        useCase.invoke(GUEST_USER_ID)

        coVerify(exactly = 0) { dictionaryRepository.getAllDictionariesByUser(any()) }
        coVerify(exactly = 0) { dictionaryRepository.reassignOwner(any(), any()) }
        coVerify(exactly = 0) { wordRepository.markWordsUnsyncedInDictionaries(any()) }
    }
}
