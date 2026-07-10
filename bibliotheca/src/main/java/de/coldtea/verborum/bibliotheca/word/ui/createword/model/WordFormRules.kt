package de.coldtea.verborum.bibliotheca.word.ui.createword.model

private val articlesByLanguage = mapOf(
    "de" to listOf("der", "die", "das"),
    "fr" to listOf("le", "la"),
    "es" to listOf("el", "la"),
    "it" to listOf("il", "la"),
    "pt" to listOf("o", "a"),
    "nl" to listOf("de", "het"),
)

private val languagesWithFeminineAdjectives = setOf("fr", "es", "it", "pt")

fun articleOptions(languageCode: String, wordType: WordType): List<String> =
    if (wordType == WordType.NOUN) articlesByLanguage[languageCode.lowercase()].orEmpty()
    else emptyList()

fun showsPluralField(wordType: WordType): Boolean = wordType == WordType.NOUN

fun showsFeminineField(languageCode: String, wordType: WordType): Boolean =
    wordType == WordType.ADJECTIVE && languageCode.lowercase() in languagesWithFeminineAdjectives

fun composeWordText(input: WordFormInput): String = listOfNotNull(
    input.article?.takeIf { it.isNotBlank() },
    input.text.trim(),
).joinToString(separator = " ")

fun composeWordMeta(languageCode: String, wordType: WordType, input: WordFormInput): String {
    val metaParts = buildList {
        add(languageCode.lowercase())
        wordType.metaType?.let { add("type=$it") }
        input.plural.trim().takeIf { it.isNotBlank() }?.let { add("plural=$it") }
        input.feminine.trim().takeIf { it.isNotBlank() }?.let { add("feminine=$it") }
    }

    return "{${metaParts.joinToString(separator = ";")}}"
}
