package de.coldtea.verborum.bibliotheca.imported.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryTagApi
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionary
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionaryContent
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedWord
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.core.utils.ApiTimestamp
import retrofit2.HttpException
import java.util.Locale
import javax.inject.Inject

/**
 * Reads the owner's current version of a public dictionary from ms_dictionary: the dictionary, its
 * tags and its words (integration doc §3, P4-10). Null when it is no longer readable — made private
 * or deleted (404). Any other failure throws, so a flaky network never looks like a removal.
 */
class FetchImportedDictionaryApiUseCase @Inject constructor(
    private val dictionaryApi: DictionaryApi,
    private val dictionaryTagApi: DictionaryTagApi,
    private val wordApi: WordApi,
) {
    suspend fun invoke(dictionaryId: String, publisherName: String?, importedAt: Long): ImportedDictionaryContent? {
        val dictionary = try {
            dictionaryApi.getDictionary(dictionaryId)
        } catch (e: HttpException) {
            if (e.code() == HTTP_NOT_FOUND) return null
            throw e
        }
        val tags = dictionaryTagApi.getDictionaryTags(dictionaryId).orEmpty()
            .mapNotNull { it.tag?.trim()?.lowercase(Locale.ROOT)?.takeIf(String::isNotEmpty) }
            .distinct()
        val words = wordApi.getWordsByDictionary(dictionaryId).orEmpty()
            .filterNot { it.wordId.isNullOrBlank() }
            .map { word ->
                val createdAt = ApiTimestamp.parse(word.createdAt) ?: importedAt
                ImportedWord(
                    wordId = word.wordId.orEmpty(),
                    dictionaryId = dictionaryId,
                    word = word.word.orEmpty(),
                    wordMeta = word.wordMeta.orEmpty(),
                    translation = word.translation.orEmpty(),
                    translationMeta = word.translationMeta.orEmpty(),
                    // The server's level is the owner's (always null for us); progress starts at 0.
                    level = 0,
                    createdAt = createdAt,
                    updatedAt = ApiTimestamp.parse(word.updatedAt) ?: createdAt,
                )
            }

        return ImportedDictionaryContent(
            dictionary = ImportedDictionary(
                dictionaryId = dictionary.dictionaryId,
                publisherId = dictionary.userId,
                publisherName = publisherName,
                name = dictionary.name,
                fromLang = dictionary.fromLang,
                toLang = dictionary.toLang,
                tags = tags,
                importedAt = importedAt,
            ),
            words = words,
        )
    }

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
