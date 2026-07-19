package de.coldtea.verborum.core.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Tracks whether the device currently has usable internet, for the offline banner.
 *
 * Registers a [ConnectivityManager.NetworkCallback] for the lifetime of the composition. Uses
 * `registerNetworkCallback` rather than `registerDefaultNetworkCallback` because the latter is
 * API 24 and this app supports 23.
 *
 * Errs towards "online": if connectivity cannot be determined the banner stays hidden, since
 * wrongly telling someone they are offline is worse than staying quiet.
 */
@Composable
fun rememberIsOnline(): Boolean {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(context.hasInternet()) }

    DisposableEffect(context) {
        val manager = context.connectivityManager()
            ?: return@DisposableEffect onDispose { }

        val callback = object : ConnectivityManager.NetworkCallback() {
            // Every signal re-reads the active network rather than trusting the per-network event,
            // so switching between Wi-Fi and mobile does not momentarily read as offline.
            override fun onAvailable(network: Network) {
                isOnline = context.hasInternet()
            }

            override fun onLost(network: Network) {
                isOnline = context.hasInternet()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                isOnline = context.hasInternet()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        runCatching { manager.registerNetworkCallback(request, callback) }

        onDispose {
            runCatching { manager.unregisterNetworkCallback(callback) }
        }
    }

    return isOnline
}

private fun Context.connectivityManager(): ConnectivityManager? =
    getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

/**
 * Requires VALIDATED as well as INTERNET so a Wi-Fi network that cannot actually reach the
 * internet (captive portals, dead hotspots) is reported as offline — which is what sync sees.
 */
private fun Context.hasInternet(): Boolean {
    val manager = connectivityManager() ?: return true
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
