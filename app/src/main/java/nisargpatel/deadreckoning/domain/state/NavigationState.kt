package nisargpatel.deadreckoning.domain.state

import nisargpatel.deadreckoning.domain.model.NavigationMode

data class NavigationState(
    val mode: NavigationMode = NavigationMode.GNSS_INS,
    val speedKmh: Double = 0.0,
    val headingDegrees: Double = 0.0,
    val accuracyMeters: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val totalDistanceKm: Double = 0.0,
    val outageDurationSeconds: Long = 0L,
    val confidencePercentage: Int = 100,
    val isNavigating: Boolean = false,
    val isDemoMode: Boolean = false
)
