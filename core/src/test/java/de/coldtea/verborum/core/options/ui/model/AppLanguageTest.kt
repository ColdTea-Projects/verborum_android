package de.coldtea.verborum.core.options.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `every language the app is translated into is offered exactly once`() {
        // One entry per values-XX locale (18) plus the base values/ (en).
        assertEquals(19, AppLanguage.entries.size)
        assertEquals(AppLanguage.entries.size, AppLanguage.entries.map { it.code }.toSet().size)
    }

    @Test
    fun `codes are plain lowercase language subtags`() {
        assertTrue(AppLanguage.entries.all { it.code == it.code.lowercase() && it.code.length == 2 })
    }

    @Test
    fun `english leads the list so the default sits at the top`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.entries.first())
    }

    @Test
    fun `a stored tag resolves on its language subtag alone`() {
        assertEquals(AppLanguage.GERMAN, AppLanguage.fromTag("de"))
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromTag("pt-BR"))
        assertEquals(AppLanguage.CHINESE, AppLanguage.fromTag("zh-Hans-CN"))
        assertEquals(AppLanguage.TURKISH, AppLanguage.fromTag("TR"))
    }

    @Test
    fun `an empty or unknown tag means system default`() {
        // getApplicationLocales() returns an empty tag list when no override is set.
        assertNull(AppLanguage.fromTag(""))
        assertNull(AppLanguage.fromTag(null))
        assertNull(AppLanguage.fromTag("sv"))
    }
}
