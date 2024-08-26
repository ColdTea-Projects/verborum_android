package de.coldtea.verborum.bibliotheca.word.ui.model

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi

sealed class DictionaryState

object Failed: DictionaryState()
object Loading: DictionaryState()

data class Success(val dictionaryUi: DictionaryUi) : DictionaryState()