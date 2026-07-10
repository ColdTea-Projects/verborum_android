package de.coldtea.verborum.bibliotheca.word.ui.createword.model

/**
 * A grammatical form captured alongside the base word, beyond gender and the word itself.
 * [metaKey] is the key written into the word meta string (e.g. `plural=Äpfel`).
 *
 * The declaration order defines the order these are serialized into the meta string.
 */
enum class FieldKey(val metaKey: String) {
    PLURAL("plural"),
    FEMININE("feminine"),
    PRESENT_3RD("present"),
    PAST("past"),
    PAST_3RD("past3"),
    PARTICIPLE("participle"),
    AUXILIARY("aux"),
}
