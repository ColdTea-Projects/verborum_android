package de.coldtea.verborum.core.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


fun Context.connectivityManager(): ConnectivityManager? =
    getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

/**
 * Requires VALIDATED as well as INTERNET so a Wi-Fi network that cannot actually reach the
 * internet (captive portals, dead hotspots) is reported as offline — which is what sync sees.
 */
fun Context.hasInternet(): Boolean {
    val manager = connectivityManager() ?: return true
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
