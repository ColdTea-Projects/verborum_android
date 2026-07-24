package de.coldtea.verborum.bibliotheca.auth.domain

import android.net.Uri
import net.openid.appauth.connectivity.ConnectionBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Mirrors AppAuth's DefaultConnectionBuilder but drops the https-only check so the token exchange
 * works against the local Keycloak over plain http (`http://localhost:8180`). Debug-only — never
 * used when the endpoints are https, so production traffic stays TLS-protected.
 */
internal object InsecureConnectionBuilder : ConnectionBuilder {

    private val CONNECT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(15).toInt()
    private val READ_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10).toInt()

    override fun openConnection(uri: Uri): HttpURLConnection {
        val connection = URL(uri.toString()).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        connection.instanceFollowRedirects = false
        return connection
    }
}
