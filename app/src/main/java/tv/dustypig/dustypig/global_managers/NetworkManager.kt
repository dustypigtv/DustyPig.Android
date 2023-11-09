package tv.dustypig.dustypig.global_managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var connected = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        private val tag = "NetworkManager"

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(tag, "onAvailable")
            connected = true
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(tag, "onLost")
            connected = false
        }
    }

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun isConnected() = connected

}