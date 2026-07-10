package de.coldtea.verborum.bibliotheca.word.ui.createword.model

/**
 * Builds the stored word surface form, e.g. "der Apfel", "l'eau", or a bare "elma".
 */
fun composeWordText(languageCode: String, input: WordFormInput): String =
    LanguageGrammar.composeSurface(languageCode, input.gender, input.text)

/**
 * Serializes the grammatical detail into the opaque meta string, e.g.
 * `{de;type=noun;gender=m;plural=Äpfel}`. Order: language, type, gender, then fields in
 * [FieldKey] declaration order. Blank fields are omitted; free text carries no type.
 */
fun composeWordMeta(languageCode: String, wordType: WordType, input: WordFormInput): String {
    val metaParts = buildList {
        add(languageCode.lowercase())
        wordType.metaType?.let { add("type=$it") }
        input.gender?.let { add("gender=${it.metaCode}") }
        FieldKey.entries.forEach { key ->
            input.field(key).trim().takeIf { it.isNotBlank() }?.let { add("${key.metaKey}=$it") }
        }
    }

    return "{${metaParts.joinToString(separator = ";")}}"
}
