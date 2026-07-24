package de.coldtea.verborum.core.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for the signed-in session. Tokens live in EncryptedSharedPreferences
 * (backed by the Android Keystore) — never plain prefs (guide §5).
 *
 * [isLoggedIn] is the reactive gate the UI shell watches: it is true while a refresh token and a
 * subject are on file. An expired access token does not sign the user out — the [TokenAuthenticator]
 * refreshes on demand; only a failed refresh (or an explicit logout) clears the session.
 */
@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    private val _isLoggedIn = MutableStateFlow(hasSession())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun currentAccessToken(): String? = prefs.getString(KEY_ACCESS, null)
    fun currentRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    /** The JWT subject — the owner id uploaded with dictionaries and words (guide §4). */
    fun currentUserId(): String? = prefs.getString(KEY_SUBJECT, null)

    @Synchronized
    fun saveTokens(accessToken: String?, refreshToken: String?, subject: String?) {
        prefs.edit().apply {
            accessToken?.let { putString(KEY_ACCESS, it) }
            refreshToken?.let { putString(KEY_REFRESH, it) }
            subject?.let { putString(KEY_SUBJECT, it) }
        }.apply()
        _isLoggedIn.value = hasSession()
    }

    /** Refresh path: swaps in a fresh access token (and a rotated refresh token if returned). */
    @Synchronized
    fun updateAccessToken(accessToken: String, refreshToken: String?) {
        prefs.edit().apply {
            putString(KEY_ACCESS, accessToken)
            refreshToken?.let { putString(KEY_REFRESH, it) }
        }.apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    private fun hasSession(): Boolean =
        currentRefreshToken() != null && currentUserId() != null

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private companion object {
        const val PREFS_NAME = "verborum_auth_prefs"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_SUBJECT = "subject"
    }
}
