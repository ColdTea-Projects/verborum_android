package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.bibliotheca.common.utils.generateUUIDV4
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.GetAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

class DictionaryService @Inject constructor(
    private val getAllDictionariesUseCase: GetAllDictionariesUseCase,
    private val saveDictionaryUseCase: SaveDictionaryUseCase,
) {

    suspend fun observeDictionaries(): Flow<List<DictionaryUi>> = flow {
        getAllDictionariesUseCase
            .invoke()
            .map { it.convertToUi() }
    }

    suspend fun crateDummyDictionary() {
        saveDictionaryUseCase.invoke(
            Dictionary(
                dictionaryId = generateUUIDV4(),
                        userId = GUEST_USER_ID,
                        name = generateRandomString(),
                        isPublic = false,
                        fromLang = "DE",
                        toLang = "EN",
                        createdAt = getNowInMillis(),
                        updatedAt = getNowInMillis(),
            )
        )
    }

    fun generateRandomString(len: Int = 15): String{
        val alphanumerics = CharArray(26) { it -> (it + 97).toChar() }.toSet()
            .union(CharArray(9) { it -> (it + 48).toChar() }.toSet())
        return (0..len-1).map {
            alphanumerics.toList().random()
        }.joinToString("")
    }
}