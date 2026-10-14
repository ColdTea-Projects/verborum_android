package de.coldtea.verborum.core.push.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The id of this app install, sent as `deviceId` when registering for push and as `X-Device-Id` on
 * every ms_dictionary write, so the server can skip this device when it nudges the others.
 *
 * Stable per install: generated once and **not** regenerated on login or logout — a second account on
 * the same phone is handled by the server (holding the same FCM token proves it is the same device).
 * Not a secret, so plain preferences. Excluded from backup (backup_rules / data_extraction_rules): a
 * restored copy must not make a new phone claim the old phone's id.
 */
@Singleton
class DeviceIdStore internal constructor(
    private val prefs: SharedPreferences,
    private val newId: () -> String,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
        { UUID.randomUUID().toString() },
    )

    @Synchronized
    fun deviceId(): String =
        prefs.getString(KEY_DEVICE_ID, null) ?: newId().also { prefs.edit().putString(KEY_DEVICE_ID, it).apply() }

    /**
     * Only for a 403 on registration: the server holds this id for another account under a token we
     * do not have, so the only way forward is a new identity for this install.
     */
    @Synchronized
    fun rotate(): String = newId().also { prefs.edit().putString(KEY_DEVICE_ID, it).apply() }

    companion object {
        const val PREFS_NAME = "verborum_push"
        private const val KEY_DEVICE_ID = "device_id"
    }
}
