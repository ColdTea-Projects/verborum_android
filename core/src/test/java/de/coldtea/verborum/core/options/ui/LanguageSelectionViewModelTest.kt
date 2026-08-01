package de.coldtea.verborum.core.options.ui

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.options.domain.AppLanguageService
import de.coldtea.verborum.core.options.ui.model.AppLanguage
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageSelectionViewModelTest : BaseTest() {

    @MockK
    private lateinit var appLanguageService: AppLanguageService

    private fun viewModel(selected: AppLanguage? = null): LanguageSelectionViewModel {
        every { appLanguageService.selected() } returns selected
        justRun { appLanguageService.select(any()) }
        return LanguageSelectionViewModel(appLanguageService)
    }

    @Test
    fun `starts on the language already in force`() = runTest {
        assertEquals(AppLanguage.TURKISH, viewModel(AppLanguage.TURKISH).selected.value)
    }

    @Test
    fun `starts on system default when no override is set`() = runTest {
        assertNull(viewModel(selected = null).selected.value)
    }

    @Test
    fun `selecting a language applies it`() = runTest {
        val viewModel = viewModel(selected = null)

        viewModel.select(AppLanguage.JAPANESE)

        assertEquals(AppLanguage.JAPANESE, viewModel.selected.value)
        verify(exactly = 1) { appLanguageService.select(AppLanguage.JAPANESE) }
    }

    @Test
    fun `selecting system default clears the override`() = runTest {
        val viewModel = viewModel(AppLanguage.GERMAN)

        viewModel.select(null)

        assertNull(viewModel.selected.value)
        verify(exactly = 1) { appLanguageService.select(null) }
    }

    @Test
    fun `re-picking the current language does not re-apply it`() = runTest {
        // Applying recreates the activity; doing that for a no-op change would be a visible jolt.
        val viewModel = viewModel(AppLanguage.GERMAN)

        viewModel.select(AppLanguage.GERMAN)

        verify(exactly = 0) { appLanguageService.select(any()) }
    }
}
