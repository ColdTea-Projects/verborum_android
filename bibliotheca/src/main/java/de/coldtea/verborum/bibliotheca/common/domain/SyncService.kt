package de.coldtea.verborum.bibliotheca.common.domain

import de.coldtea.verborum.bibliotheca.common.domain.usecases.SyncUserDictionariesUseCase
import javax.inject.Inject

class SyncService @Inject constructor(
    private val syncUserDictionariesUseCase: SyncUserDictionariesUseCase,
) {

    suspend fun syncDictionaries() = syncUserDictionariesUseCase.invoke()
}