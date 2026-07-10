package de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model

sealed class CreateDictionaryState {
    data object Idle : CreateDictionaryState()
    data object Saving : CreateDictionaryState()
    data class Created(val dictionaryId: String) : CreateDictionaryState()
    data object Failed : CreateDictionaryState()
}
