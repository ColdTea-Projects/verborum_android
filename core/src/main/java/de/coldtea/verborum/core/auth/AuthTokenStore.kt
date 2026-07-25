package de.coldtea.verborum.core.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for the signed-in session. Tokens live in EncryptedSharedPreferences
 * (backed by the Android Keystore) — never plain prefs (guide §5).
 *
 * [isLoggedIn] is the reactive gate the UI shell watches: `true` while a refresh token and a
 * subject are on file, `false` when signed out, and `null` until the encrypted store has been read
 * (resolved off the main thread so Keystore setup doesn't jank startup). An expired access token
 * does not sign the user out — the [TokenAuthenticator] refreshes on demand; only a failed refresh
 * (or an explicit logout) clears the session.
 */
@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // Lazy so the (potentially slow, occasionally throwing) Keystore init happens on first touch
    // rather than at injection time on the main thread; the init block below warms it off-main.
    private val prefs: SharedPreferences by lazy { createEncryptedPrefs(context) }

    // null = not yet determined. The shell renders nothing until it resolves, avoiding a
    // login-screen flash for an already-signed-in user.
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        // App-lifetime singleton: this scope intentionally lives for the process and is never
        // cancelled. Warms the encrypted store off the main thread and seeds the login state.
        CoroutineScope(Dispatchers.IO).launch {
            _isLoggedIn.value = hasSession()
        }
    }

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

    /**
     * Builds the encrypted store, recovering from a corrupted master key / prefs — a known
     * security-crypto failure mode that otherwise throws here and crash-loops the app on every
     * launch. On failure we reset the Keystore alias + prefs file and rebuild, landing the user in
     * a clean signed-out state instead.
     */
    private fun createEncryptedPrefs(context: Context): SharedPreferences =
        runCatching { buildEncryptedPrefs(context) }
            .getOrElse {
                resetCorruptedStore(context)
                buildEncryptedPrefs(context)
            }

    // MasterKeys is the stable (security-crypto 1.0.0) API; the fluent MasterKey.Builder only
    // exists in the 1.1.0 alphas, which we deliberately avoid in the auth-critical path.
    @Suppress("DEPRECATION")
    private fun buildEncryptedPrefs(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun resetCorruptedStore(context: Context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.deleteSharedPreferences(PREFS_NAME)
            } else {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().clear().commit()
            }
        }
        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                .deleteEntry(MASTER_KEY_ALIAS)
        }
    }

    private companion object {
        const val PREFS_NAME = "verborum_auth_prefs"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_SUBJECT = "subject"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        // The default alias security-crypto's MasterKeys.getOrCreate(AES256_GCM_SPEC) creates.
        const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"
    }
}
