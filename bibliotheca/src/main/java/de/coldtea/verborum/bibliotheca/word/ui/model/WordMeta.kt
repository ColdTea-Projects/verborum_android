package de.coldtea.verborum.bibliotheca.word.ui.model

import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.Gender
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType

/**
 * Parsed form of the meta string written by the create-word screen, e.g.
 * `{de;type=verb;gender=m;past=ging;participle=gegangen;aux=sein}`. Unknown keys are ignored so
 * older and newer meta formats stay readable.
 */
data class WordMeta(
    val languageCode: String,
    val wordType: WordType?,
    val gender: Gender?,
    val fields: Map<FieldKey, String>,
) {
    fun field(key: FieldKey): String? = fields[key]?.takeIf { it.isNotBlank() }

    /**
     * A form as shown to the learner. The past participle carries its auxiliary verb, e.g.
     * "(sein) gegangen"; the auxiliary alone is not a display form.
     */
    fun displayForm(key: FieldKey): String? = displayForm(fields, key)

    /** All display forms in [FieldKey] declaration order. */
    fun displayForms(): List<String> = displayForms(fields)

    companion object {
        fun parse(meta: String): WordMeta? {
            val content = meta.trim().removeSurrounding("{", "}")
            if (content.isBlank()) return null

            val parts = content.split(";")
            var wordType: WordType? = null
            var gender: Gender? = null
            val fields = mutableMapOf<FieldKey, String>()

            parts.drop(1).forEach { part ->
                val key = part.substringBefore('=')
                val value = part.substringAfter('=', missingDelimiterValue = "")
                when (key) {
                    "type" -> wordType = WordType.entries.firstOrNull { it.metaType == value }
                    "gender" -> gender = Gender.entries.firstOrNull { it.metaCode == value }
                    else -> FieldKey.entries.firstOrNull { it.metaKey == key }
                        ?.let { fields[it] = value }
                }
            }

            return WordMeta(
                languageCode = parts.first(),
                wordType = wordType,
                gender = gender,
                fields = fields,
            )
        }
    }
}

/** [WordMeta.displayForm] over raw field values, usable before a meta string exists. */
fun displayForm(fields: Map<FieldKey, String>, key: FieldKey): String? {
    if (key == FieldKey.AUXILIARY) return null
    val value = fields[key]?.takeIf { it.isNotBlank() } ?: return null
    if (key != FieldKey.PARTICIPLE) return value
    val auxiliary = fields[FieldKey.AUXILIARY]?.takeIf { it.isNotBlank() }
    return auxiliary?.let { "($it) $value" } ?: value
}

/** All display forms of [fields] in [FieldKey] declaration order. */
fun displayForms(fields: Map<FieldKey, String>): List<String> =
    FieldKey.entries.mapNotNull { displayForm(fields, it) }

/**
 * The stored text followed by every extra grammatical form, matching the create screen's preview
 * line: "der Apfel · Äpfel", "gehen · ging · (sein) gegangen".
 */
fun displayLine(text: String, meta: String): String {
    val forms = WordMeta.parse(meta)?.displayForms().orEmpty()
    return (listOf(text) + forms).joinToString(separator = " · ")
}

fun WordUi.wordDisplayLine(): String = displayLine(word, wordMeta)

fun WordUi.translationDisplayLine(): String = displayLine(translation, translationMeta)
