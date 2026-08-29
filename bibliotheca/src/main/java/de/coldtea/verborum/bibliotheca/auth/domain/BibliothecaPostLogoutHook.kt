package de.coldtea.verborum.bibliotheca.auth.domain

import de.coldtea.verborum.bibliotheca.auth.domain.usecase.ClearLocalDataUseCase
import de.coldtea.verborum.core.auth.domain.PostLogoutHook
import javax.inject.Inject

/**
 * Bibliotheca's share of the logout lifecycle: wipe the local database so the device holds no
 * owner-keyed rows once the session is gone. Runs entirely offline — no network required.
 */
class BibliothecaPostLogoutHook @Inject constructor(
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
) : PostLogoutHook {

    override suspend fun onLoggedOut() = clearLocalDataUseCase.invoke()
}
