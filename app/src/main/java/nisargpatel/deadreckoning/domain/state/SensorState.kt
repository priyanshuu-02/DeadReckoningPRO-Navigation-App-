package nisargpatel.deadreckoning.domain.state

data class SensorState(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val accelMagnitude: Float = 0f,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val magX: Float = 0f,
    val magY: Float = 0f,
    val magZ: Float = 0f,
    val rollDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val yawDegrees: Float = 0f,
    val mountStabilityPercentage: Int = 0,
    val alignmentConfidencePercentage: Int = 0,
    val imuSamplingHz: Int = 0,
    val overallHealthPercentage: Int = 0,
    val isAccelAvailable: Boolean = true,
    val isGyroAvailable: Boolean = true,
    val isMagAvailable: Boolean = true
)
