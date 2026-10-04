package de.coldtea.verborum.forum.marketplace.ui.forumsearch

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ForumSearchViewModelTest : BaseTest() {

    private val current = ForumDictionaryFilter(
        languagePairs = listOf(ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH)),
        tags = setOf("a1"),
        userName = "anna",
    )

    private fun viewModel() = ForumSearchViewModel().apply { init(current) }

    @Test
    fun `the draft starts from the list's current filter`() {
        assertEquals(current, viewModel().draft.value)
    }

    @Test
    fun `a second init does not overwrite the edits, as after a rotation`() {
        val viewModel = viewModel()
        viewModel.onUserNameChange("kenji")

        viewModel.init(ForumDictionaryFilter())

        assertEquals("kenji", viewModel.draft.value.userName)
    }

    @Test
    fun `every criterion is editable`() {
        val viewModel = viewModel()

        viewModel.onRemoveLanguagePair(ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH))
        viewModel.onAddLanguagePair(SupportedLanguage.FRENCH, SupportedLanguage.ENGLISH)
        viewModel.onToggleTag("a1")
        viewModel.onToggleTag("travel")
        viewModel.onUserNameChange("kenji")

        assertEquals(
            ForumDictionaryFilter(
                languagePairs = listOf(ForumLanguagePair(SupportedLanguage.FRENCH, SupportedLanguage.ENGLISH)),
                tags = setOf("travel"),
                userName = "kenji",
            ),
            viewModel.draft.value,
        )
    }

    @Test
    fun `pairs are appended in the order they are added`() {
        val viewModel = viewModel()

        viewModel.onAddLanguagePair(SupportedLanguage.FRENCH, SupportedLanguage.ENGLISH)

        assertEquals(
            listOf(
                ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH),
                ForumLanguagePair(SupportedLanguage.FRENCH, SupportedLanguage.ENGLISH),
            ),
            viewModel.draft.value.languagePairs,
        )
    }

    @Test
    fun `a pair already chosen in either order is not added twice`() {
        val viewModel = viewModel()

        viewModel.onAddLanguagePair(SupportedLanguage.TURKISH, SupportedLanguage.GERMAN)
        viewModel.onAddLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH)

        assertEquals(1, viewModel.draft.value.languagePairs.size)
    }

    @Test
    fun `a language cannot be paired with itself`() {
        val viewModel = viewModel()

        viewModel.onAddLanguagePair(SupportedLanguage.FRENCH, SupportedLanguage.FRENCH)

        assertEquals(1, viewModel.draft.value.languagePairs.size)
    }

    @Test
    fun `clear empties the draft`() {
        val viewModel = viewModel()

        viewModel.clear()

        assertEquals(ForumDictionaryFilter(), viewModel.draft.value)
    }

    @Test
    fun `the search filter trims the user name and drops a blank one`() {
        val viewModel = viewModel()

        viewModel.onUserNameChange("  anna ")
        assertEquals("anna", viewModel.searchFilter().userName)

        viewModel.onUserNameChange("   ")
        assertNull(viewModel.searchFilter().userName)
    }

    // region backend limits
    @Test
    fun `no more than ten language pairs are added`() {
        val viewModel = ForumSearchViewModel().apply { init(ForumDictionaryFilter()) }
        val languages = SupportedLanguage.entries

        languages.drop(1).take(11).forEach { viewModel.onAddLanguagePair(languages.first(), it) }

        assertEquals(10, viewModel.draft.value.languagePairs.size)
    }

    @Test
    fun `no more than ten tags are selected, but a selected one can still be removed`() {
        val viewModel = ForumSearchViewModel().apply { init(ForumDictionaryFilter()) }

        (1..11).forEach { viewModel.onToggleTag("tag$it") }
        assertEquals(10, viewModel.draft.value.tags.size)

        viewModel.onToggleTag("tag1")
        assertEquals(9, viewModel.draft.value.tags.size)
    }

    @Test
    fun `the user name is capped at the backend limit`() {
        val viewModel = viewModel()

        viewModel.onUserNameChange("a".repeat(300))

        assertEquals(255, viewModel.draft.value.userName?.length)
    }
    // endregion
}
