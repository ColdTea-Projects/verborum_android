package de.coldtea.verborum.bibliotheca.dictionary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.local.CleanDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.core.ui.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryListViewModel @Inject constructor(
    private val dictionaryService: DictionaryService,
    private val syncService: SyncService,
    private val cleanDictionariesUseCase: CleanDictionariesUseCase,
) : BaseViewModel() {

    private val _dictionariesState = MutableStateFlow(listOf<DictionaryUi>())
    val dictionariesState = _dictionariesState.asStateFlow()

    init {
        viewModelScope.launch {
            dictionaryService.observeDictionaries().observe(
                onSuccess = { dictionary ->
                    _dictionariesState.emit(dictionary)
                }
            )
            syncService.syncDictionaries()
        }
    }

    fun addDummyDictionary() = viewModelScope.launch {
        dictionaryService.crateDummyDictionary()
    }

    fun cleanDictionaries() = viewModelScope.launch {
        dictionariesState.value.map {
            async { dictionaryService.deleteDictionary(it.dictionaryId) }
        }.awaitAll()
        cleanDictionariesUseCase.invoke()//TODO:replace with removal at sync
        syncService.syncDictionaries()
    }
}