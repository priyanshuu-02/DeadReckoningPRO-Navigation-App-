package nisargpatel.deadreckoning.domain.state

data class AIState(
    val isModelLoaded: Boolean = false,
    val isActive: Boolean = false,
    val predictedSpeedKmh: Double = 0.0,
    val speedConfidencePercentage: Int = 0,
    val motionClassification: String = "UNKNOWN",
    val motionConfidencePercentage: Int = 0,
    val anomalyDetected: String = "None",
    val inferenceTimeMs: Long = 0L,
    val modelVersion: String = "v1.0-mock"
)
