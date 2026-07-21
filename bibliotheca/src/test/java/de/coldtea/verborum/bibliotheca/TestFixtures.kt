package de.coldtea.verborum.bibliotheca

import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryResponse
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.word.data.api.model.WordResponse
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun testWordEntity(
    wordId: String = "word-id",
    dictionaryId: String = "dict-id",
    word: String = "apple",
    wordMeta: String = "{en}",
    translation: String = "der Apfel",
    translationMeta: String = "{de}",
    isSynced: Boolean = false,
    isDeleted: Boolean = false,
    level: Int = 0,
    createdAt: Long = 1_000L,
    updatedAt: Long = 2_000L,
) = WordEntity(
    wordId = wordId,
    dictionaryId = dictionaryId,
    word = word,
    wordMeta = wordMeta,
    translation = translation,
    translationMeta = translationMeta,
    isSynced = isSynced,
    isDeleted = isDeleted,
    level = level,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun testWord(
    wordId: String = "word-id",
    dictionaryId: String = "dict-id",
    word: String = "apple",
    wordMeta: String = "{en}",
    translation: String = "der Apfel",
    translationMeta: String = "{de}",
    isSynced: Boolean = false,
    isDeleted: Boolean = false,
    level: Int = 0,
    createdAt: Long = 1_000L,
    updatedAt: Long = 2_000L,
) = Word(
    wordId = wordId,
    dictionaryId = dictionaryId,
    word = word,
    wordMeta = wordMeta,
    translation = translation,
    translationMeta = translationMeta,
    isSynced = isSynced,
    isDeleted = isDeleted,
    level = level,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun testWordUi(
    wordId: String = "word-id",
    dictionaryId: String = "dict-id",
    word: String = "apple",
    wordMeta: String = "{en}",
    translation: String = "der Apfel",
    translationMeta: String = "{de}",
    level: Int = 0,
    createdAt: Long = 1_000L,
    updatedAt: Long = 2_000L,
) = WordUi(
    wordId = wordId,
    dictionaryId = dictionaryId,
    word = word,
    wordMeta = wordMeta,
    translation = translation,
    translationMeta = translationMeta,
    level = level,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun testWordResponse(
    wordId: String? = "word-id",
    word: String? = "apple",
    wordMeta: String? = "{en}",
    translation: String? = "der Apfel",
    translationMeta: String? = "{de}",
    // Defaults to a valid level so responses look "synced"; pass null to simulate a backend that
    // omits it, or an out-of-range JsonPrimitive to simulate bad data.
    level: JsonElement? = JsonPrimitive(0),
) = WordResponse(
    wordId = wordId,
    word = word,
    wordMeta = wordMeta,
    translation = translation,
    translationMeta = translationMeta,
    level = level,
)

fun testDictionary(
    dictionaryId: String = "dict-id",
    userId: String = "user-id",
    name: String = "My Dictionary",
    isPublic: Boolean = false,
    isSynced: Boolean = false,
    isDeleted: Boolean = false,
    fromLang: String = "en",
    toLang: String = "de",
    createdAt: Long = 1_000L,
    updatedAt: Long = 2_000L,
) = Dictionary(
    dictionaryId = dictionaryId,
    userId = userId,
    name = name,
    isPublic = isPublic,
    isSynced = isSynced,
    isDeleted = isDeleted,
    fromLang = fromLang,
    toLang = toLang,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun testDictionaryEntity(
    dictionaryId: String = "dict-id",
    userId: String = "user-id",
    name: String = "My Dictionary",
    isPublic: Boolean = false,
    isSynced: Boolean = false,
    isDeleted: Boolean = false,
    fromLang: String = "en",
    toLang: String = "de",
    createdAt: Long = 1_000L,
    updatedAt: Long = 2_000L,
) = DictionaryEntity(
    dictionaryId = dictionaryId,
    userId = userId,
    name = name,
    isPublic = isPublic,
    isSynced = isSynced,
    isDeleted = isDeleted,
    fromLang = fromLang,
    toLang = toLang,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun testDictionaryResponse(
    dictionaryId: String = "dict-id",
    userId: String = "user-id",
    name: String = "My Dictionary",
    isPublic: Boolean = false,
    fromLang: String = "en",
    toLang: String = "de",
) = DictionaryResponse(
    dictionaryId = dictionaryId,
    userId = userId,
    name = name,
    isPublic = isPublic,
    fromLang = fromLang,
    toLang = toLang,
)

fun testDictionaryUi(
    dictionaryId: String = "dict-id",
    userId: String = "user-id",
    name: String = "My Dictionary",
    isPublic: Boolean = false,
    fromLang: String = "en",
    toLang: String = "de",
    createdAt: Long = 1_000L,
    updatedAt: Long = 2_000L,
) = DictionaryUi(
    dictionaryId = dictionaryId,
    userId = userId,
    name = name,
    isPublic = isPublic,
    fromLang = fromLang,
    toLang = toLang,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
