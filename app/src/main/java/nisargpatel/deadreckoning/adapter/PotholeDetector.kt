package nisargpatel.deadreckoning.adapter

import android.util.Log
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PotholeDetectionResult(
    val detected: Boolean,
    val severity: String,
    val confidence: Int
)

class PotholeDetector {
    companion object {
        private const val TAG = "PotholeDetector"
        private const val WINDOW_SIZE = 5
        private const val MIN_SHOCK_SCORE = 10.5f
        private const val MIN_VIBRATION_SCORE = 6.0f
        private const val SINGLE_SAMPLE_SHOCK = 18.0f
        private const val SINGLE_SAMPLE_VIBRATION = 10.5f
    }

    private val accelWindow = ArrayDeque<Float>()
    private val gyroWindow = ArrayDeque<Float>()
    private val shockWindow = ArrayDeque<Float>()
    private val vibrationWindow = ArrayDeque<Float>()

    fun update(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float
    ): PotholeDetectionResult {
        val accelMagnitude = sqrt((accelX * accelX) + (accelY * accelY) + (accelZ * accelZ).toDouble()).toFloat()
        val gyroMagnitude = sqrt((gyroX * gyroX) + (gyroY * gyroY) + (gyroZ * gyroZ).toDouble()).toFloat()

        accelWindow.addLast(accelMagnitude)
        gyroWindow.addLast(gyroMagnitude)
        shockWindow.addLast(abs(accelMagnitude - 9.8f))
        vibrationWindow.addLast(abs(gyroMagnitude - 0.25f))

        while (accelWindow.size > 10) accelWindow.removeFirst()
        while (gyroWindow.size > 10) gyroWindow.removeFirst()
        while (shockWindow.size > WINDOW_SIZE) shockWindow.removeFirst()
        while (vibrationWindow.size > WINDOW_SIZE) vibrationWindow.removeFirst()

        val meanAccel = accelWindow.average().toFloat()
        val meanGyro = gyroWindow.average().toFloat()
        val accelVariance = accelWindow.map { val diff = it - meanAccel; diff * diff }.average()
        val gyroVariance = gyroWindow.map { val diff = it - meanGyro; diff * diff }.average()
        val accelStd = sqrt(accelVariance).toFloat()
        val gyroStd = sqrt(gyroVariance).toFloat()

        val accelDelta = abs(accelMagnitude - 9.8f)
        val gyroDelta = abs(gyroMagnitude - 0.25f)
        val shockScore = accelDelta + (accelStd * 2.5f)
        val vibrationScore = (gyroStd * 3.1f) + (gyroDelta * 2.2f)

        val avgShock = shockWindow.average().toFloat()
        val avgVibration = vibrationWindow.average().toFloat()
        val hasStrongImpact = accelDelta > 4.5f || accelMagnitude > 14.5f || accelMagnitude < 6.2f
        val hasStrongRotation = gyroMagnitude > 2.2f || avgVibration > MIN_VIBRATION_SCORE

        val strongImpactSamples = shockWindow.count { it > 4.5f }
        val strongRotationSamples = vibrationWindow.count { it > 2.0f }
        val severeImpactSamples = shockWindow.count { it > 6.5f }

        val multiSampleHit = shockWindow.size >= 3 &&
            strongImpactSamples >= 3 &&
            strongRotationSamples >= 3 &&
            avgShock > 5.5f &&
            avgVibration > 2.4f
        val singleHit = shockScore > SINGLE_SAMPLE_SHOCK && vibrationScore > SINGLE_SAMPLE_VIBRATION
        val isPothole = hasStrongImpact && hasStrongRotation && (singleHit || multiSampleHit)
        val severeCluster = multiSampleHit &&
            severeImpactSamples >= 2 &&
            avgShock > 6.3f &&
            avgVibration > 2.8f

        val severity = when {
            !isPothole -> "None"
            severeCluster -> "Severe"
            shockScore > 15.0f || vibrationScore > 8.8f -> "Moderate"
            shockScore > MIN_SHOCK_SCORE && vibrationScore > MIN_VIBRATION_SCORE -> "Minor"
            else -> "Minor"
        }

        val confidence = calculateConfidence(
            severity = severity,
            shockScore = shockScore,
            vibrationScore = vibrationScore,
            avgShock = avgShock,
            avgVibration = avgVibration,
            strongImpactSamples = strongImpactSamples,
            strongRotationSamples = strongRotationSamples
        )

        if (isPothole) {
            try {
                Log.i(TAG, "Pothole detected | severity=$severity | confidence=$confidence% | shock=${String.format("%.2f", shockScore)} | vibration=${String.format("%.2f", vibrationScore)} | timestamp=${System.currentTimeMillis()}")
            } catch (e: Exception) {
                // Logging may not be available in unit tests
            }
        }

        return PotholeDetectionResult(
            detected = isPothole,
            severity = severity,
            confidence = confidence
        )
    }

    private fun calculateConfidence(
        severity: String,
        shockScore: Float,
        vibrationScore: Float,
        avgShock: Float,
        avgVibration: Float,
        strongImpactSamples: Int,
        strongRotationSamples: Int
    ): Int {
        if (severity == "None") return 0

        val shockStrength = ((shockScore - MIN_SHOCK_SCORE) / 24f).coerceIn(0f, 1f)
        val vibrationStrength = ((vibrationScore - MIN_VIBRATION_SCORE) / 18f).coerceIn(0f, 1f)
        val avgShockStrength = ((avgShock - 5.5f) / 14f).coerceIn(0f, 1f)
        val avgVibrationStrength = ((avgVibration - 2.4f) / 10f).coerceIn(0f, 1f)
        val sampleStrength = ((strongImpactSamples + strongRotationSamples - 4) / 6f).coerceIn(0f, 1f)

        val confidenceStrength =
            (shockStrength * 0.32f) +
            (vibrationStrength * 0.28f) +
            (avgShockStrength * 0.18f) +
            (avgVibrationStrength * 0.12f) +
            (sampleStrength * 0.10f)

        val range = when (severity) {
            "Severe" -> 93..99
            "Moderate" -> 78..92
            else -> 60..77
        }

        return (range.first + (confidenceStrength * (range.last - range.first))).roundToInt()
            .coerceIn(range.first, range.last)
    }
}
