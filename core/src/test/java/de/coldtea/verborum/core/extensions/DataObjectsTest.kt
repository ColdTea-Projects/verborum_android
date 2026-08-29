package de.coldtea.verborum.core.extensions

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class DataObjectsTest {

    private val obj = json.parseToJsonElement(
        """
        {
            "name": "Yasar",
            "count": 3,
            "nested": {"inner": "value"},
            "list": ["a", "b"],
            "empty": null
        }
        """
    ).jsonObject

    @Test
    fun `stringOrNull returns the value of a string primitive`() {
        assertEquals("Yasar", obj.stringOrNull("name"))
    }

    @Test
    fun `stringOrNull returns the content of a non-string primitive`() {
        assertEquals("3", obj.stringOrNull("count"))
    }

    @Test
    fun `stringOrNull returns null for an absent key`() {
        assertNull(obj.stringOrNull("missing"))
    }

    @Test
    fun `stringOrNull returns null for a JSON null`() {
        assertNull(obj.stringOrNull("empty"))
    }

    // The documented contract: anything that is not a primitive is null, never a throw — a JWT
    // claim holding an object or array (nonstandard IdP mapping, hostile token) must not crash.
    @Test
    fun `stringOrNull returns null for an object value`() {
        assertNull(obj.stringOrNull("nested"))
    }

    @Test
    fun `stringOrNull returns null for an array value`() {
        assertNull(obj.stringOrNull("list"))
    }
}
