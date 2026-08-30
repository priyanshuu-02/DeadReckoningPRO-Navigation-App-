package nisargpatel.deadreckoning.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import nisargpatel.deadreckoning.domain.state.GNSSState

/**
 * Adapter bridging existing location implementation into GNSSState flow.
 * Does NOT implement new GNSS outage algorithms.
 */
class LocationAdapter(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _gnssState = MutableStateFlow(GNSSState())
    val gnssState: StateFlow<GNSSState> = _gnssState.asStateFlow()

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setMinUpdateIntervalMillis(500L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            handleLocation(location)
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            _gnssState.value = _gnssState.value.copy(isAvailable = false, fixStatus = "NO PERMISSION")
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun handleLocation(location: Location) {
        val speedKmh = location.speed * 3.6
        val accuracy = location.accuracy.toDouble()
        val fixStatus = if (accuracy < 15.0) "3D FIX (GPS)" else if (accuracy < 30.0) "2D FIX" else "LOW FIX"
        val quality = ((100.0 - accuracy * 2).coerceIn(10.0, 99.0)).toInt()

        _gnssState.value = _gnssState.value.copy(
            isAvailable = true,
            accuracyMeters = accuracy,
            latitude = location.latitude,
            longitude = location.longitude,
            speedKmh = speedKmh,
            bearingDegrees = location.bearing.toDouble(),
            signalQualityPercentage = quality,
            fixStatus = fixStatus
        )
    }
}
