package com.example.goatly.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

fun Context.observeConnectivity(): Flow<Boolean> = callbackFlow {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { trySend(true) }
        override fun onLost(network: Network) { trySend(false) }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    manager.registerNetworkCallback(request, callback)

    // Emit current state immediately
    val activeNetwork = manager.activeNetwork
    val caps = manager.getNetworkCapabilities(activeNetwork)
    trySend(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)

    awaitClose { manager.unregisterNetworkCallback(callback) }
}.distinctUntilChanged()
