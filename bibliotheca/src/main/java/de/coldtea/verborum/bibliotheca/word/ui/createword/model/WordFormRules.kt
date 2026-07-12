package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.bibliotheca.word.ui.model.WordMeta
import de.coldtea.verborum.bibliotheca.word.ui.model.WordMetaDto
import de.coldtea.verborum.bibliotheca.word.ui.model.parseBundle
import de.coldtea.verborum.bibliotheca.word.ui.model.splitSurfaces
import de.coldtea.verborum.bibliotheca.word.ui.model.wordMetaJson
import kotlinx.serialization.encodeToString

/**
 * Builds the stored word surface column as a JSON array of per-meaning surfaces, e.g.
 * `["der Apfel"]` or `["kaufen","erwerben"]`. JSON escaping keeps any user text safe —
 * including "/" — and the array stays index-aligned with the meta's field lists. Blank
 * meanings are dropped.
 */
fun composeWordText(languageCode: String, inputs: List<WordFormInput>): String =
    inputs
        .map { LanguageGrammar.composeSurface(languageCode, it.gender, it.text).trim() }
        .filter { it.isNotBlank() }
        .let { wordMetaJson.encodeToString(it) }

fun composeWordText(languageCode: String, input: WordFormInput): String =
    composeWordText(languageCode, listOf(input))

/**
 * Serializes the grammatical detail of every meaning into the JSON meta blob, e.g.
 * `{"lang":"de","type":"verb","fields":{"past":["kaufte","erwarb"],"aux":["haben","haben"]}}`.
 * Field lists are index-aligned across meanings; a key blank in every meaning is omitted, and free
 * text carries no type. The base surface forms live separately (see [composeWordText]).
 */
fun composeWordMeta(languageCode: String, wordType: WordType, inputs: List<WordFormInput>): String {
    val genders = inputs.map { it.gender?.metaCode.orEmpty() }
    val fields = linkedMapOf<String, List<String>>()
    FieldKey.entries.forEach { key ->
        val values = inputs.map { it.field(key).trim() }
        if (values.any { it.isNotBlank() }) fields[key.metaKey] = values
    }

    val dto = WordMetaDto(
        lang = languageCode.lowercase(),
        type = wordType.metaType,
        genders = if (genders.any { it.isNotBlank() }) genders else emptyList(),
        fields = fields,
    )
    return wordMetaJson.encodeToString(dto)
}

fun composeWordMeta(languageCode: String, wordType: WordType, input: WordFormInput): String =
    composeWordMeta(languageCode, wordType, listOf(input))

/**
 * Inverse of [composeWordText] + [composeWordMeta]: rebuilds the editable form inputs (one per
 * meaning) from a stored word and its meta, so the create screen can prefill in edit mode.
 */
fun parseWordFormInputs(languageCode: String, storedText: String, meta: String): List<WordFormInput> {
    val surfaces = splitSurfaces(storedText)
    val meanings = parseBundle(meta)?.meanings.orEmpty()
    val count = maxOf(surfaces.size, meanings.size).coerceAtLeast(1)

    return (0 until count).map { i ->
        WordFormInput(
            text = surfaces.getOrNull(i)
                ?.let { LanguageGrammar.extractBaseWord(languageCode, it) }
                .orEmpty(),
            gender = meanings.getOrNull(i)?.gender,
            fields = meanings.getOrNull(i)?.fields.orEmpty(),
        )
    }
}

fun parseWordFormInput(languageCode: String, storedText: String, meta: String): WordFormInput =
    parseWordFormInputs(languageCode, storedText, meta).firstOrNull() ?: WordFormInput()

/** The word type recorded in the meta; metas without a type are free text. */
fun parseWordType(meta: String): WordType =
    WordMeta.parse(meta)?.wordType ?: WordType.FREE_TEXT
