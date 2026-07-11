package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.bibliotheca.word.ui.model.WordMeta

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

/**
 * Inverse of [composeWordText] + [composeWordMeta]: rebuilds the editable form input from a
 * stored word and its meta, so the create screen can prefill in edit mode.
 */
fun parseWordFormInput(languageCode: String, storedText: String, meta: String): WordFormInput {
    val parsed = WordMeta.parse(meta)
    return WordFormInput(
        text = LanguageGrammar.extractBaseWord(languageCode, storedText),
        gender = parsed?.gender,
        fields = parsed?.fields.orEmpty(),
    )
}

/** The word type recorded in the meta; metas without a type are free text. */
fun parseWordType(meta: String): WordType =
    WordMeta.parse(meta)?.wordType ?: WordType.FREE_TEXT
