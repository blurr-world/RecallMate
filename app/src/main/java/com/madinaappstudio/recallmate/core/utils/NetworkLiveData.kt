package com.madinaappstudio.recallmate.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData

class NetworkLiveData(context: Context) : LiveData<Boolean>() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            update(network)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            update(network, networkCapabilities)
        }

        override fun onLost(network: Network) {
            postValueIfChanged(false)
        }
    }

    override fun onActive() {
        super.onActive()

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        postValueIfChanged(hasInternet(capabilities))

        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(callback)
    }

    private fun update(
        network: Network,
        capabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(network)
    ) {
        postValueIfChanged(hasInternet(capabilities))
    }

    private fun hasInternet(capabilities: NetworkCapabilities?): Boolean {
        return capabilities?.let {
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: false
    }

    private fun postValueIfChanged(newValue: Boolean) {
        if (value != newValue) {
            postValue(newValue)
        }
    }
}
