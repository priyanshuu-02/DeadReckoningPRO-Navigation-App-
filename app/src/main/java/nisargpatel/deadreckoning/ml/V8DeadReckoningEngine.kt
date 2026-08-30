package nisargpatel.deadreckoning.ml

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.google.gson.Gson
import java.nio.FloatBuffer
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

data class V8Prediction(
    val speedMps: Float,
    val forwardMeters: Float,
    val lateralMeters: Float,
    val headingDeltaRadians: Float,
    val motionClass: MotionClass,
    val confidencePercentage: Int,
    val inferenceTimeMs: Long
)

enum class MotionClass(val label: String) {
    STATIONARY("Stationary"),
    STRAIGHT("Driving straight"),
    TURNING("Turning");

    companion object {
        fun fromIndex(index: Int) = entries.getOrElse(index) { STRAIGHT }
    }
}

private data class V8Normalization(
    val imu_mean: List<Float>,
    val imu_std: List<Float>,
    val speed_mean: Float,
    val speed_std: Float,
    val position_mean: List<Float>,
    val position_std: List<Float>
)

/**
 * Runs the reviewed V8 model on non-overlapping 2-second IMU windows.
 * Non-overlap matters here: V8's displacement output describes the whole window.
 */
class V8DeadReckoningEngine(context: Context) : AutoCloseable {
    companion object {
        private const val TAG = "V8DeadReckoning"
        private const val WINDOW_SIZE = 20
        private const val CHANNEL_COUNT = 6
        private const val SAMPLE_INTERVAL_NS = 100_000_000L
    }

    private val environment = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val normalization: V8Normalization
    private val window = ArrayDeque<FloatArray>(WINDOW_SIZE)
    private val rawWindow = ArrayDeque<FloatArray>(WINDOW_SIZE)
    private var lastAcceptedTimestampNs = 0L
    private var samplesSincePrediction = 0

    init {
        val model = context.assets.open("ml/v8_dead_reckoning.onnx").use { it.readBytes() }
        normalization = context.assets.open("ml/v8_normalization.json").reader().use {
            Gson().fromJson(it, V8Normalization::class.java)
        }
        session = environment.createSession(model, OrtSession.SessionOptions())
        Log.i(TAG, "Loaded V8 ONNX model with a 20 x 6 IMU input window")
    }

    fun addSample(
        timestampNs: Long,
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float,
        initialSpeedMps: Float
    ): V8Prediction? {
        if (lastAcceptedTimestampNs != 0L && timestampNs - lastAcceptedTimestampNs < SAMPLE_INTERVAL_NS) return null
        lastAcceptedTimestampNs = timestampNs
        val raw = floatArrayOf(accelX, accelY, accelZ, gyroX, gyroY, gyroZ)
        val normalized = FloatArray(CHANNEL_COUNT) { index ->
            (raw[index] - normalization.imu_mean[index]) / normalization.imu_std[index]
        }
        if (window.size == WINDOW_SIZE) window.removeFirst()
        if (rawWindow.size == WINDOW_SIZE) rawWindow.removeFirst()
        window.addLast(normalized)
        rawWindow.addLast(raw)
        samplesSincePrediction++
        if (window.size < WINDOW_SIZE || samplesSincePrediction < WINDOW_SIZE) return null
        samplesSincePrediction = 0
        return infer(initialSpeedMps)
    }

    private fun infer(initialSpeedMps: Float): V8Prediction {
        val startedAt = System.nanoTime()
        val imu = FloatArray(WINDOW_SIZE * CHANNEL_COUNT)
        window.forEachIndexed { sampleIndex, sample ->
            sample.copyInto(imu, sampleIndex * CHANNEL_COUNT)
        }
        val normalizedSpeed = (initialSpeedMps - normalization.speed_mean) / normalization.speed_std
        val imuTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(imu), longArrayOf(1, WINDOW_SIZE.toLong(), CHANNEL_COUNT.toLong()))
        val speedTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(floatArrayOf(normalizedSpeed)), longArrayOf(1))
        imuTensor.use { input ->
            speedTensor.use { state ->
                session.run(mapOf("imu" to input, "initial_speed_normalized" to state)).use { output ->
                    val speed = (output["speed"]!!.get().value as FloatArray)[0] * normalization.speed_std + normalization.speed_mean
                    val position = (output["position"]!!.get().value as Array<FloatArray>)[0]
                    val headingDelta = (output["heading_delta"]!!.get().value as FloatArray)[0]
                    val logits = (output["motion_logits"]!!.get().value as Array<FloatArray>)[0]
                    val probabilities = softmax(logits)
                    val motionIndex = probabilities.indices.maxBy { probabilities[it] }
                    val prediction = V8Prediction(
                        speedMps = speed.coerceAtLeast(0f),
                        forwardMeters = position[0] * normalization.position_std[0] + normalization.position_mean[0],
                        lateralMeters = position[1] * normalization.position_std[1] + normalization.position_mean[1],
                        headingDeltaRadians = headingDelta,
                        motionClass = MotionClass.fromIndex(motionIndex),
                        confidencePercentage = (probabilities[motionIndex] * 100).toInt().coerceIn(0, 100),
                        inferenceTimeMs = (System.nanoTime() - startedAt) / 1_000_000L
                    )
                    if (isStationary()) {
                        return prediction.copy(
                            speedMps = 0f,
                            forwardMeters = 0f,
                            lateralMeters = 0f,
                            headingDeltaRadians = 0f,
                            motionClass = MotionClass.STATIONARY,
                            confidencePercentage = 95
                        )
                    }
                    Log.d(TAG, "Inference: ${prediction.motionClass.label}, speed=${prediction.speedMps * 3.6f} km/h, confidence=${prediction.confidencePercentage}%, ${prediction.inferenceTimeMs} ms")
                    return prediction
                }
            }
        }
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val highest = logits.maxOrNull() ?: 0f
        val unnormalized = FloatArray(logits.size) { exp((logits[it] - highest).toDouble()).toFloat() }
        val total = unnormalized.sum().coerceAtLeast(0.0001f)
        return FloatArray(logits.size) { unnormalized[it] / total }
    }

    private fun isStationary(): Boolean {
        val averageLinearAcceleration = rawWindow.map { sample ->
            abs(sqrt(sample[0] * sample[0] + sample[1] * sample[1] + sample[2] * sample[2]) - 9.81f)
        }.average()
        val averageRotation = rawWindow.map { sample ->
            sqrt(sample[3] * sample[3] + sample[4] * sample[4] + sample[5] * sample[5])
        }.average()
        return averageLinearAcceleration < 0.35 && averageRotation < 0.12
    }

    override fun close() = session.close()
}
