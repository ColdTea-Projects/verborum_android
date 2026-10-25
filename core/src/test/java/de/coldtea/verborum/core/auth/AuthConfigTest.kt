package de.coldtea.verborum.core.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthConfigTest {

    @Test
    fun `the re-authentication request forces a fresh sign-in with max_age 0 and nothing else`() {
        // Account deletion needs a login under 5 minutes old (SEC-13); max_age=0 is how the browser is told so
        assertEquals(mapOf("max_age" to "0"), AuthConfig.REAUTHENTICATION_PARAMETERS)
    }
}
