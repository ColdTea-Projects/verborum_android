package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model

import de.coldtea.verborum.bibliotheca.R
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.DictionaryDeletion
import de.coldtea.verborum.core.ui.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DictionaryDeletionMessageTest {

    @Test
    fun `only a refusal is reported`() {
        assertNull(deletionMessage(DictionaryDeletion.DELETED))
        assertNull(deletionMessage(DictionaryDeletion.PENDING))
        assertEquals(UiText.Resource(R.string.errorDeleteLastShared), deletionMessage(DictionaryDeletion.SHARING_REQUIRED))
        assertEquals(UiText.Resource(R.string.errorDeleteFailed), deletionMessage(DictionaryDeletion.REFUSED))
    }
}
