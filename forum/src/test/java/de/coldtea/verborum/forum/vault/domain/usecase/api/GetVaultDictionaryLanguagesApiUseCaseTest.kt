package de.coldtea.verborum.forum.vault.domain.usecase.api

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import de.coldtea.verborum.forum.marketplace.data.api.model.SharedDictionaryResponse
import de.coldtea.verborum.forum.vault.data.VaultRepository
import de.coldtea.verborum.forum.vault.data.api.model.VaultEntryResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetVaultDictionaryLanguagesApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var repository: VaultRepository

    private lateinit var subject: GetVaultDictionaryLanguagesApiUseCase

    override fun setUp() {
        super.setUp()
        subject = GetVaultDictionaryLanguagesApiUseCase(repository)
    }

    private fun dictionary(id: String, from: String, to: String) =
        SharedDictionaryResponse(dictionaryId = id, userId = "owner", name = id, fromLang = from, toLang = to)

    @Test
    fun `reads the imported dictionaries in one batch, with the import time as activity`() = runTest {
        coEvery { repository.getVault("user-id") } returns listOf(
            VaultEntryResponse(dictionaryId = "a", importedAt = "1970-01-01T00:00:01Z"),
            VaultEntryResponse(dictionaryId = "gone", importedAt = "1970-01-01T00:00:02Z"),
        )
        // "gone" became private: the batch leaves it out.
        coEvery { repository.getDictionaries(listOf("a", "gone")) } returns listOf(dictionary("a", "DE", "EN"))

        assertEquals(listOf(DictionaryLanguages("DE", "EN", 1_000L)), subject("user-id"))
    }

    @Test
    fun `an empty vault needs no dictionary read`() = runTest {
        coEvery { repository.getVault("user-id") } returns emptyList()

        assertEquals(emptyList<DictionaryLanguages>(), subject("user-id"))
        coVerify(exactly = 0) { repository.getDictionaries(any()) }
    }
}
