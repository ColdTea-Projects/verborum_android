package de.coldtea.verborum.bibliotheca.common.domain

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.domain.usecases.SyncUserDictionariesUseCase
import de.coldtea.verborum.bibliotheca.common.domain.usecases.UploadPendingChangesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncService @Inject constructor(
    private val uploadPendingChangesUseCase: UploadPendingChangesUseCase,
    private val syncUserDictionariesUseCase: SyncUserDictionariesUseCase,
) {

    suspend fun syncDictionaries() = withContext(Dispatchers.IO) {
        // Upload local changes first so the download merge cannot drop data the server
        // has never seen. Each phase fails independently.
        uploadPendingChanges()
        try {
            syncUserDictionariesUseCase.invoke()
        } catch (ex: Exception) {
            Log.e("Sync error", ex.message ?: "")
        }
    }

    /**
     * Just the upload half — push local changes to the server without pulling the whole dataset
     * back. Used by the immediate, change-triggered sync so getting data *up* stays cheap; the
     * download-merge is left to the periodic reconcile and screen-open sync.
     */
    suspend fun uploadPendingChanges() = withContext(Dispatchers.IO) {
        try {
            uploadPendingChangesUseCase.invoke()
        } catch (ex: Exception) {
            Log.e("Upload error", ex.message ?: "")
        }
    }
}
