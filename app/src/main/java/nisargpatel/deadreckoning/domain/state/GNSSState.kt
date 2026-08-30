package nisargpatel.deadreckoning.domain.state

data class GNSSState(
    val isAvailable: Boolean = false,
    val satelliteCount: Int = 0,
    val accuracyMeters: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speedKmh: Double = 0.0,
    val bearingDegrees: Double = 0.0,
    val signalQualityPercentage: Int = 0,
    val fixStatus: String = "NO FIX",
    val outageDurationSeconds: Long = 0L,
    val hdop: Float = 0.0f,
    val vdop: Float = 0.0f
)
