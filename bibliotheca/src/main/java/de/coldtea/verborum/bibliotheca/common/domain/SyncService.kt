package de.coldtea.verborum.bibliotheca.common.domain

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.domain.usecases.SyncUserDictionariesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncService @Inject constructor(
    private val syncUserDictionariesUseCase: SyncUserDictionariesUseCase,
) {

    suspend fun syncDictionaries() = withContext(Dispatchers.IO) {
        try {
            syncUserDictionariesUseCase.invoke()
        }catch (ex: Exception){
            Log.e("Sync error", ex.message?:"")
        }
    }
}