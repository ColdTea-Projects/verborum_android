package de.coldtea.verborum.core.ui

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.coldtea.verborum.core.extensions.connectivityManager
import de.coldtea.verborum.core.extensions.hasInternet
import de.coldtea.verborum.core.extensions.isUsable

/**
 * The app-wide connectivity state, provided once by the navigation root from [rememberIsOnline] so
 * screens can react to it without each registering its own network callback. Defaults to online
 * (previews, tests).
 */
val LocalIsOnline = compositionLocalOf { true }

/**
 * Tracks whether the device currently has usable internet, for the offline banner.
 *
 * Registers a [ConnectivityManager.NetworkCallback] for the lifetime of the composition. Uses
 * `registerNetworkCallback` rather than `registerDefaultNetworkCallback` because the latter is
 * API 24 and this app supports 23.
 *
 * Online means at least one network is INTERNET + VALIDATED, tracked from the callbacks so losing the
 * last network (flight mode) is seen immediately.
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

        // The callback's own bookkeeping is the source of truth after the initial read. Re-reading
        // `activeNetwork` inside onLost is a race: it can still return the network that is going
        // away (flight mode, Wi-Fi switched off), which wrongly reported "online".
        val usable = mutableSetOf<Network>()

        val callback = object : ConnectivityManager.NetworkCallback() {
            // Nothing to do in onAvailable: capabilities (and so VALIDATED) follow in
            // onCapabilitiesChanged, so a captive portal never counts and startup never flickers.

            override fun onLost(network: Network) {
                usable.remove(network)
                isOnline = usable.isNotEmpty()
            }

            override fun onUnavailable() {
                isOnline = usable.isNotEmpty()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                if (networkCapabilities.isUsable()) usable.add(network) else usable.remove(network)
                isOnline = usable.isNotEmpty()
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
