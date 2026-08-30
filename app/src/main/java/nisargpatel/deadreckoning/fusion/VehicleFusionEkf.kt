package nisargpatel.deadreckoning.fusion

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import org.osmdroid.util.GeoPoint

data class FusedVehicleState(
    val position: GeoPoint,
    val speedMps: Double,
    val headingDegrees: Double,
    val horizontalUncertaintyMeters: Double
)

/**
 * Lightweight error-state EKF for the phone's vehicle frame. V8 supplies the
 * propagation delta and GNSS supplies position, speed, and heading measurements.
 */
class VehicleFusionEkf {
    private var reference: GeoPoint? = null
    private var eastMeters = 0.0
    private var northMeters = 0.0
    private var speedMps = 0.0
    private var headingRadians = 0.0
    private var positionVariance = 400.0
    private var speedVariance = 25.0
    private var headingVariance = Math.toRadians(35.0).let { it * it }

    fun isInitialized() = reference != null

    fun reset(position: GeoPoint, speedMps: Double, headingDegrees: Double, accuracyMeters: Double) {
        reference = position
        eastMeters = 0.0
        northMeters = 0.0
        this.speedMps = speedMps.coerceAtLeast(0.0)
        headingRadians = Math.toRadians(headingDegrees)
        positionVariance = accuracyMeters.coerceAtLeast(3.0).let { it * it }
        speedVariance = 4.0
        headingVariance = Math.toRadians(15.0).let { it * it }
    }

    fun predict(forwardMeters: Double, lateralMeters: Double, headingDeltaRadians: Double, intervalSeconds: Double): FusedVehicleState? {
        if (reference == null) return null
        headingRadians = normalizeRadians(headingRadians + headingDeltaRadians)
        val north = forwardMeters * cos(headingRadians) - lateralMeters * sin(headingRadians)
        val east = forwardMeters * sin(headingRadians) + lateralMeters * cos(headingRadians)
        northMeters += north
        eastMeters += east
        speedMps = (forwardMeters / intervalSeconds.coerceAtLeast(0.1)).coerceAtLeast(0.0)
        positionVariance += 2.5 + abs(lateralMeters) * 2.0
        speedVariance += 0.8
        headingVariance += Math.toRadians(2.0).let { it * it }
        return state()
    }

    /** IMU-rate attitude propagation between the lower-rate V8 displacement windows. */
    fun predictGyro(angularVelocityZRadPerSec: Double, intervalSeconds: Double): FusedVehicleState? {
        if (reference == null || intervalSeconds <= 0.0 || intervalSeconds > 0.25) return null
        headingRadians = normalizeRadians(headingRadians + angularVelocityZRadPerSec * intervalSeconds)
        headingVariance += Math.toRadians(0.6).let { it * it }
        return state()
    }

    fun updateGnss(position: GeoPoint, speedMps: Double, headingDegrees: Double, accuracyMeters: Double): FusedVehicleState {
        if (reference == null) {
            reset(position, speedMps, headingDegrees, accuracyMeters)
            return state()
        }
        val measurement = toLocal(position)
        val measurementVariance = accuracyMeters.coerceAtLeast(3.0).let { it * it }
        val positionGain = positionVariance / (positionVariance + measurementVariance)
        eastMeters += positionGain * (measurement.first - eastMeters)
        northMeters += positionGain * (measurement.second - northMeters)
        positionVariance *= 1.0 - positionGain

        val speedGain = speedVariance / (speedVariance + 2.25)
        this.speedMps += speedGain * (speedMps.coerceAtLeast(0.0) - this.speedMps)
        speedVariance *= 1.0 - speedGain

        if (speedMps >= 1.5) {
            val measurementHeading = Math.toRadians(headingDegrees)
            val headingGain = headingVariance / (headingVariance + Math.toRadians(12.0).let { it * it })
            headingRadians = normalizeRadians(headingRadians + headingGain * shortestDelta(headingRadians, measurementHeading))
            headingVariance *= 1.0 - headingGain
        }
        return state()
    }

    fun state(): FusedVehicleState {
        val ref = checkNotNull(reference)
        val latitude = ref.latitude + northMeters / 111_111.0
        val longitude = ref.longitude + eastMeters / (111_111.0 * cos(Math.toRadians(ref.latitude)))
        return FusedVehicleState(
            position = GeoPoint(latitude, longitude),
            speedMps = speedMps,
            headingDegrees = (Math.toDegrees(headingRadians) + 360.0) % 360.0,
            horizontalUncertaintyMeters = kotlin.math.sqrt(positionVariance).coerceAtLeast(1.0)
        )
    }

    private fun toLocal(position: GeoPoint): Pair<Double, Double> {
        val ref = checkNotNull(reference)
        val north = (position.latitude - ref.latitude) * 111_111.0
        val east = (position.longitude - ref.longitude) * 111_111.0 * cos(Math.toRadians(ref.latitude))
        return east to north
    }

    private fun shortestDelta(from: Double, to: Double): Double = normalizeRadians(to - from)
    private fun normalizeRadians(value: Double): Double = ((value + Math.PI) % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI) - Math.PI
}
