package de.coldtea.verborum.bibliotheca.word.ui.model

import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.Gender
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Separator between alternative meanings in presentation ("kaufen/erwerben"). */
const val ALT_SEPARATOR = "/"

/**
 * Parsed form of the meta blob written by the create-word screen. A word can carry several
 * alternative meanings (e.g. *kaufen/erwerben*); each [WordMetaBundle.Meaning] is one alternative.
 *
 * [WordMeta] is the single-meaning view used by callers that only need the first alternative
 * (the quiz, [de.coldtea.verborum.bibliotheca.word.ui.createword.model.parseWordType]); it is
 * derived from the first meaning of [parseBundle].
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
            val bundle = parseBundle(meta) ?: return null
            val first = bundle.meanings.firstOrNull()
            return WordMeta(
                languageCode = bundle.languageCode,
                wordType = bundle.wordType,
                gender = first?.gender,
                fields = first?.fields.orEmpty(),
            )
        }
    }
}

/**
 * All alternative meanings parsed from a meta blob. [languageCode] and [wordType] are shared by
 * every meaning; each [Meaning] carries its own gender and grammatical fields.
 */
data class WordMetaBundle(
    val languageCode: String,
    val wordType: WordType?,
    val meanings: List<Meaning>,
) {
    data class Meaning(
        val gender: Gender?,
        val fields: Map<FieldKey, String>,
    )
}

/** JSON shape of the meta blob: `lang`/`type` scalar, every other value a per-meaning list. */
@Serializable
internal data class WordMetaDto(
    val lang: String,
    val type: String? = null,
    val genders: List<String> = emptyList(),
    val fields: Map<String, List<String>> = emptyMap(),
)

internal val wordMetaJson = Json { encodeDefaults = false; ignoreUnknownKeys = true }

/**
 * Reads the JSON meta blob (e.g. `{"lang":"de","type":"verb","fields":{"past":["kaufte","erwarb"]}}`)
 * into its meanings. Field lists are index-aligned with the surfaces stored in the word column.
 * Unknown keys are ignored so newer meta formats stay readable; unparseable metas yield null.
 */
fun parseBundle(meta: String): WordMetaBundle? {
    val trimmed = meta.trim()
    if (trimmed.isEmpty()) return null
    val dto = runCatching { wordMetaJson.decodeFromString<WordMetaDto>(trimmed) }.getOrNull() ?: return null

    val count = maxOf(dto.genders.size, dto.fields.values.maxOfOrNull { it.size } ?: 0).coerceAtLeast(1)
    val meanings = (0 until count).map { i ->
        val gender = dto.genders.getOrNull(i)?.takeIf { it.isNotBlank() }
            ?.let { code -> Gender.entries.firstOrNull { it.metaCode == code } }
        val fields = buildMap {
            dto.fields.forEach { (metaKey, values) ->
                val key = FieldKey.entries.firstOrNull { it.metaKey == metaKey } ?: return@forEach
                values.getOrNull(i)?.trim()?.takeIf { it.isNotBlank() }?.let { put(key, it) }
            }
        }
        WordMetaBundle.Meaning(gender, fields)
    }

    val wordType = dto.type?.let { type -> WordType.entries.firstOrNull { it.metaType == type } }
    return WordMetaBundle(dto.lang, wordType, meanings)
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
 * Splits a stored surface column into its alternative surfaces. Surfaces are stored as a JSON
 * array (`["buy","purchase"]`) so user text may safely contain any character, including "/".
 * Anything that isn't a JSON array is treated as one bare surface.
 */
fun splitSurfaces(text: String): List<String> {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return emptyList()
    if (trimmed.startsWith("[")) {
        runCatching { wordMetaJson.decodeFromString<List<String>>(trimmed) }
            .getOrNull()
            ?.let { surfaces -> return surfaces.map(String::trim).filter(String::isNotBlank) }
    }
    return listOf(trimmed)
}

/** The stored surface column as shown to the learner: `["buy","purchase"]` → "buy/purchase". */
fun surfacesDisplay(text: String): String = splitSurfaces(text).joinToString(ALT_SEPARATOR)

/**
 * One presentation line from surfaces plus each meaning's grammatical fields: alternatives are
 * joined by "/" within each slot, slots by " · " — "kaufen/erwerben · kaufte/erwarb · …".
 * Shared by the stored-word renderers and the create screen's live preview.
 */
fun displayLine(surfaces: List<String>, meaningsFields: List<Map<FieldKey, String>>): String {
    val baseColumn = surfaces.filter { it.isNotBlank() }.joinToString(ALT_SEPARATOR)
    val formColumns = FieldKey.entries.mapNotNull { key ->
        meaningsFields.mapNotNull { displayForm(it, key) }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(ALT_SEPARATOR)
    }
    return (listOf(baseColumn) + formColumns).filter { it.isNotBlank() }.joinToString(separator = " · ")
}

/** [displayLine] over the stored columns of a saved word. */
fun displayLine(text: String, meta: String): String =
    displayLine(
        surfaces = splitSurfaces(text),
        meaningsFields = parseBundle(meta)?.meanings?.map { it.fields }.orEmpty(),
    )

fun WordUi.wordDisplayLine(): String = displayLine(word, wordMeta)

fun WordUi.translationDisplayLine(): String = displayLine(translation, translationMeta)
