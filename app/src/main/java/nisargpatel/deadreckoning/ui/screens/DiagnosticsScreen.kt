package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.DiagnosticsViewModel

@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel
) {
    val navState by viewModel.navigationState.collectAsState()
    val sensorState by viewModel.sensorState.collectAsState()
    val gnssState by viewModel.gnssState.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.BugReport, contentDescription = "Diagnostics", tint = WarningAmber, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "DEVELOPER DIAGNOSTICS", color = WarningAmber, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                Text(text = "Hardware Sampling Rates, Engine Latency & Runtime Telemetry", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Frequency & Sampling Rates Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "HARDWARE SAMPLING FREQUENCY", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "IMU Sampling Rate (Accel/Gyro)", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "${sensorState.imuSamplingHz} Hz", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "GNSS Positioning Rate", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "1 Hz", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Algorithm Latency Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleAI.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "PIPELINE LATENCY (MOCK DEMO METRICS)", color = PurpleAI, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "On-Device AI Inference Latency", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "${aiState.inferenceTimeMs} ms", color = PurpleAI, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "EKF Sensor Fusion Step Latency", color = TextSecondary, fontSize = 12.sp)
                    // TODO Phase 4: Replace mock EKF metrics with real fusion engine metrics.
                    Text(text = "2 ms", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Map Matching Calculation Latency", color = TextSecondary, fontSize = 12.sp)
                    // TODO Phase 5: Replace mock map matching metrics with real map matching metrics.
                    Text(text = "5 ms", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App Runtime Performance Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "APP RUNTIME & SYSTEM TELEMETRY", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "UI Frame Rate", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "59 FPS", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Heap Memory Usage", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "182 MB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Battery Status", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "78% (Charging)", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
