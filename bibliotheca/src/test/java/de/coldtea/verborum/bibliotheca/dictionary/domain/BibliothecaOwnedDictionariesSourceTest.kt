package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BibliothecaOwnedDictionariesSourceTest : BaseTest() {

    @MockK
    private lateinit var getAllDictionariesUseCase: GetAllDictionariesUseCase

    private lateinit var subject: BibliothecaOwnedDictionariesSource

    override fun setUp() {
        super.setUp()
        subject = BibliothecaOwnedDictionariesSource(getAllDictionariesUseCase)
    }

    @Test
    fun `exposes the languages of every dictionary that is not deleted`() = runTest {
        coEvery { getAllDictionariesUseCase.invoke() } returns listOf(
            testDictionary(dictionaryId = "a", fromLang = "de", toLang = "en", createdAt = 1_000L, updatedAt = 5_000L),
            testDictionary(dictionaryId = "b", fromLang = "tr", toLang = "en", isDeleted = true),
            // Never stamped by the server yet: its creation time is its latest activity.
            testDictionary(dictionaryId = "c", fromLang = "fr", toLang = "en", createdAt = 3_000L, updatedAt = 0L),
        )

        assertEquals(
            listOf(DictionaryLanguages("de", "en", 5_000L), DictionaryLanguages("fr", "en", 3_000L)),
            subject.getOwnedDictionaryLanguages(),
        )
    }
}
