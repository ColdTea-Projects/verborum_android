package de.coldtea.verborum.core.push.data

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DeviceIdStoreTest {

    /** An in-memory stand-in for the one key the store uses. */
    private class FakePrefs {
        var stored: String? = null
        val prefs: SharedPreferences = mockk {
            every { getString("device_id", null) } answers { stored }
            every { edit() } answers {
                mockk<SharedPreferences.Editor> {
                    every { putString("device_id", any()) } answers {
                        stored = secondArg()
                        this@mockk
                    }
                    every { apply() } returns Unit
                }
            }
        }
    }

    private fun store(fake: FakePrefs): DeviceIdStore {
        var counter = 0
        return DeviceIdStore(fake.prefs) { "id-${++counter}" }
    }

    @Test
    fun `the id is generated once and then stays stable`() {
        val store = store(FakePrefs())

        val first = store.deviceId()

        assertEquals("id-1", first)
        assertEquals(first, store.deviceId())
        assertEquals(first, store.deviceId())
    }

    @Test
    fun `an id already on file is reused after a restart`() {
        val fake = FakePrefs().apply { stored = "persisted-id" }

        assertEquals("persisted-id", store(fake).deviceId())
    }

    @Test
    fun `rotate replaces the id and later reads see the new one`() {
        val store = store(FakePrefs())
        val before = store.deviceId()

        val rotated = store.rotate()

        assertNotEquals(before, rotated)
        assertEquals(rotated, store.deviceId())
    }
}
