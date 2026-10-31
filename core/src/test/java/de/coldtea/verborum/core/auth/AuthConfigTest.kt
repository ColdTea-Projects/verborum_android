package de.coldtea.verborum.core.auth

import de.coldtea.verborum.core.options.ui.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthConfigTest {

    @Test
    fun `the re-authentication request forces a fresh sign-in with max_age 0 and nothing else`() {
        // Account deletion needs a login under 5 minutes old (SEC-13); max_age=0 is how the browser is told so
        assertEquals(mapOf("max_age" to "0"), AuthConfig.REAUTHENTICATION_PARAMETERS)
    }

    @Test
    fun `the login page language is the pickers choice first`() {
        assertEquals("de", AuthConfig.uiLocalesFor(AppLanguage.GERMAN, "tr-TR"))
    }

    @Test
    fun `without a choice the device language is used and a regional tag is cut to the language`() {
        assertEquals("pt", AuthConfig.uiLocalesFor(null, "pt-BR"))
        assertEquals("zh", AuthConfig.uiLocalesFor(null, "zh-Hans-CN"))
    }

    @Test
    fun `a device language the app does not have falls back to English`() {
        assertEquals("en", AuthConfig.uiLocalesFor(null, "sv-SE"))
        assertEquals("en", AuthConfig.uiLocalesFor(null, null))
    }

    @Test
    fun `every app language is sent under its own code`() {
        AppLanguage.entries.forEach { assertEquals(it.code, AuthConfig.uiLocalesFor(it, "en")) }
    }
}
