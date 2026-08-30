package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.components.HealthIndicator
import nisargpatel.deadreckoning.ui.components.MetricCard
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.SensorsViewModel

@Composable
fun SensorsScreen(
    viewModel: SensorsViewModel
) {
    val sensorState by viewModel.sensorState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Sensors, contentDescription = "Sensors", tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "IMU SENSOR DASHBOARD", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                Text(text = "Accelerometer • Gyroscope • Magnetometer • Orientation", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HealthIndicator(healthPercentage = sensorState.overallHealthPercentage, label = "Overall IMU Sensor Health")

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Orientation & Mount Stability Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ScreenRotation, contentDescription = "Mount", tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "PHONE ORIENTATION & VEHICLE MOUNT", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Roll", color = TextSecondary, fontSize = 11.sp)
                        Text(text = String.format("%.1f°", sensorState.rollDegrees), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                        Text(text = "Pitch", color = TextSecondary, fontSize = 11.sp)
                        Text(text = String.format("%.1f°", sensorState.pitchDegrees), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                        Text(text = "Yaw", color = TextSecondary, fontSize = 11.sp)
                        Text(text = String.format("%.1f°", sensorState.yawDegrees), color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Mount Stability", color = TextSecondary, fontSize = 11.sp)
                        Text(text = "${sensorState.mountStabilityPercentage}%", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accelerometer Card
        SensorDetailCard(
            title = "ACCELEROMETER (3-AXIS)",
            samplingHz = sensorState.imuSamplingHz,
            v1 = "X: ${String.format("%.2f", sensorState.accelX)} m/s²",
            v2 = "Y: ${String.format("%.2f", sensorState.accelY)} m/s²",
            v3 = "Z: ${String.format("%.2f", sensorState.accelZ)} m/s²",
            magnitude = "Mag: ${String.format("%.2f", sensorState.accelMagnitude)} m/s²"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gyroscope Card
        SensorDetailCard(
            title = "GYROSCOPE (ANGULAR VELOCITY)",
            samplingHz = sensorState.imuSamplingHz,
            v1 = "X: ${String.format("%.3f", sensorState.gyroX)} rad/s",
            v2 = "Y: ${String.format("%.3f", sensorState.gyroY)} rad/s",
            v3 = "Z: ${String.format("%.3f", sensorState.gyroZ)} rad/s",
            magnitude = "Status: CALIBRATED"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Magnetometer Card
        SensorDetailCard(
            title = "MAGNETOMETER (3-AXIS)",
            samplingHz = 50,
            v1 = "X: ${String.format("%.1f", sensorState.magX)} µT",
            v2 = "Y: ${String.format("%.1f", sensorState.magY)} µT",
            v3 = "Z: ${String.format("%.1f", sensorState.magZ)} µT",
            magnitude = "Heading: ${String.format("%.0f°", sensorState.yawDegrees)}"
        )
    }
}

@Composable
private fun SensorDetailCard(
    title: String,
    samplingHz: Int,
    v1: String,
    v2: String,
    v3: String,
    magnitude: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "$samplingHz Hz", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = v1, color = TextPrimary, fontSize = 12.sp)
                Text(text = v2, color = TextPrimary, fontSize = 12.sp)
                Text(text = v3, color = TextPrimary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = magnitude, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
