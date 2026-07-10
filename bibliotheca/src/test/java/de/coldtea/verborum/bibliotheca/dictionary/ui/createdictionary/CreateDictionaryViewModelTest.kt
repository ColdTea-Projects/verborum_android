package de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary

import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.CreateDictionaryState
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateDictionaryViewModelTest : BaseTest() {

    @MockK
    private lateinit var dictionaryService: DictionaryService

    private lateinit var viewModel: CreateDictionaryViewModel

    override fun setUp() {
        super.setUp()
        viewModel = CreateDictionaryViewModel(dictionaryService)
    }

    // region initial state

    @Test
    fun `initial createDictionaryState is Idle`() = runTest {
        assertEquals(CreateDictionaryState.Idle, viewModel.createDictionaryState.first())
    }

    // endregion

    // region create

    @Test
    fun `create emits Created with the id returned by the service`() = runTest {
        coEvery { dictionaryService.createDictionary(any(), any(), any()) } returns "dict-42"

        viewModel.create(
            name = "German Basics",
            fromLang = SupportedLanguage.GERMAN,
            toLang = SupportedLanguage.ENGLISH,
        )

        assertEquals(
            CreateDictionaryState.Created("dict-42"),
            viewModel.createDictionaryState.first(),
        )
    }

    @Test
    fun `create passes trimmed name and language codes to the service`() = runTest {
        coEvery { dictionaryService.createDictionary(any(), any(), any()) } returns "dict-42"

        viewModel.create(
            name = "  German Basics  ",
            fromLang = SupportedLanguage.GERMAN,
            toLang = SupportedLanguage.ENGLISH,
        )

        coVerify(exactly = 1) {
            dictionaryService.createDictionary(
                name = "German Basics",
                fromLang = "de",
                toLang = "en",
            )
        }
    }

    @Test
    fun `create emits Failed when the service throws`() = runTest {
        coEvery {
            dictionaryService.createDictionary(any(), any(), any())
        } throws RuntimeException("db error")

        viewModel.create(
            name = "German Basics",
            fromLang = SupportedLanguage.GERMAN,
            toLang = SupportedLanguage.ENGLISH,
        )

        assertEquals(CreateDictionaryState.Failed, viewModel.createDictionaryState.first())
    }

    // endregion
}
