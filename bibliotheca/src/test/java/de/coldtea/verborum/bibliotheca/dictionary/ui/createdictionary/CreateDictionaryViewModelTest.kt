package de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary

import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.CreateDictionaryState
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.testDictionaryUi
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        viewModel.save(
            name = "German Basics",
            fromLang = SupportedLanguage.GERMAN,
            toLang = SupportedLanguage.ENGLISH,
            tags = emptyList(),
        )

        assertEquals(
            CreateDictionaryState.Created("dict-42"),
            viewModel.createDictionaryState.first(),
        )
    }

    @Test
    fun `create passes trimmed name and language codes to the service`() = runTest {
        coEvery { dictionaryService.createDictionary(any(), any(), any()) } returns "dict-42"

        viewModel.save(
            name = "  German Basics  ",
            fromLang = SupportedLanguage.GERMAN,
            toLang = SupportedLanguage.ENGLISH,
            tags = emptyList(),
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

        viewModel.save(
            name = "German Basics",
            fromLang = SupportedLanguage.GERMAN,
            toLang = SupportedLanguage.ENGLISH,
            tags = emptyList(),
        )

        assertEquals(CreateDictionaryState.Failed, viewModel.createDictionaryState.first())
    }

    // endregion

    // region edit mode

    @Test
    fun `init loads the dictionary to edit`() = runTest {
        val dictionary = testDictionaryUi(dictionaryId = "dict-1", name = "German Basics")
        coEvery { dictionaryService.getDictionary("dict-1") } returns dictionary

        viewModel.init("dict-1")

        assertEquals(dictionary, viewModel.editingDictionary.first())
    }

    @Test
    fun `init with a null id stays in create mode`() = runTest {
        viewModel.init(null)

        assertNull(viewModel.editingDictionary.first())
    }

    @Test
    fun `save in edit mode updates the loaded dictionary and emits Updated`() = runTest {
        val dictionary = testDictionaryUi(
            dictionaryId = "dict-1",
            name = "German Basics",
            fromLang = "de",
            toLang = "en",
        )
        coEvery { dictionaryService.getDictionary("dict-1") } returns dictionary
        coEvery { dictionaryService.updateDictionary(any()) } returns "dict-1"
        viewModel.init("dict-1")

        val updatedSlot = slot<DictionaryUi>()

        viewModel.save(
            name = "  German Advanced  ",
            fromLang = SupportedLanguage.GERMAN,
            toLang = SupportedLanguage.SPANISH,
            tags = emptyList(),
        )

        coVerify(exactly = 1) { dictionaryService.updateDictionary(capture(updatedSlot)) }
        coVerify(exactly = 0) { dictionaryService.createDictionary(any(), any(), any()) }
        val updated = updatedSlot.captured
        assertEquals("dict-1", updated.dictionaryId)
        assertEquals("German Advanced", updated.name)
        assertEquals("de", updated.fromLang)
        assertEquals("es", updated.toLang)
        assertEquals(CreateDictionaryState.Updated, viewModel.createDictionaryState.first())
    }

    // endregion
}
