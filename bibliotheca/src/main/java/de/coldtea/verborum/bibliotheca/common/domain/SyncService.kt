package de.coldtea.verborum.bibliotheca.common.domain

import javax.inject.Inject

class SyncService @Inject constructor(
    private val syncUserDictionariesUseCase: SyncUserDictionariesUseCase,
) {

    suspend fun syncDictionaries() = syncUserDictionariesUseCase.invoke()
}