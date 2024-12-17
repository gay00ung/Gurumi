package com.gurumi.weather.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient

object LocationUtils {

    private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as android.app.Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun getLastLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onLocationReceived: (latitude: Double, longitude: Double) -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (checkLocationPermission(context)) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationReceived(location.latitude, location.longitude)
                    }
                }
        } else {
            onPermissionDenied()
        }
    }
}
