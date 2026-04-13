package com.example.goatly.data.network

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.*

object LocationManager {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, onResult: (lat: Double, lng: Double) -> Unit) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                }
            }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        context: Context,
        onUpdate: (lat: Double, lng: Double) -> Unit
    ): LocationCallback {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onUpdate(location.latitude, location.longitude)
                }
            }
        }

        client.requestLocationUpdates(request, callback, context.mainLooper)
        return callback
    }

    fun stopLocationUpdates(context: Context, callback: LocationCallback) {
        LocationServices.getFusedLocationProviderClient(context)
            .removeLocationUpdates(callback)
    }

    fun distanceInMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371000.0
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLambda = Math.toRadians(lng2 - lng1)
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun formatDistance(meters: Double): String {
        return if (meters < 1000) "A ${meters.toInt()} m de tu ubicación"
        else "A ${"%.1f".format(meters / 1000)} km de tu ubicación"
    }
}