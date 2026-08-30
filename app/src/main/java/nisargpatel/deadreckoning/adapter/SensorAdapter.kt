package nisargpatel.deadreckoning.adapter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import nisargpatel.deadreckoning.domain.state.SensorState
import kotlin.math.sqrt

/**
 * Adapter bridging existing Android SensorManager into Kotlin SensorState flow
 * without modifying sensor processing algorithms.
 */
class SensorAdapter(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _sensorState = MutableStateFlow(
        SensorState(
            isAccelAvailable = accelSensor != null,
            isGyroAvailable = gyroSensor != null,
            isMagAvailable = magSensor != null
        )
    )
    val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()

    private var gravityValues = FloatArray(3)
    private var magValues = FloatArray(3)

    fun startListening() {
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        magSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val current = _sensorState.value
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravityValues = event.values.clone()
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]
                val mag = sqrt(ax * ax + ay * ay + az * az)
                _sensorState.value = current.copy(
                    accelX = ax, accelY = ay, accelZ = az, accelMagnitude = mag
                )
                updateOrientation()
            }
            Sensor.TYPE_GYROSCOPE -> {
                _sensorState.value = current.copy(
                    gyroX = event.values[0], gyroY = event.values[1], gyroZ = event.values[2]
                )
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magValues = event.values.clone()
                _sensorState.value = current.copy(
                    magX = event.values[0], magY = event.values[1], magZ = event.values[2]
                )
                updateOrientation()
            }
        }
    }

    private fun updateOrientation() {
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityValues, magValues)) {
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val yaw = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            _sensorState.value = _sensorState.value.copy(
                rollDegrees = roll,
                pitchDegrees = pitch,
                yawDegrees = yaw
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
