package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.components.AIStatusCard
import nisargpatel.deadreckoning.ui.components.MetricCard
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.IntelligenceViewModel

@Composable
fun IntelligenceScreen(
    viewModel: IntelligenceViewModel
) {
    val aiState by viewModel.aiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI", tint = PurpleAI, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "ON-DEVICE AI INTELLIGENCE", color = PurpleAI, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                Text(text = "Real-Time IMU Speed & Motion Inference Engine", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main AI Status Header Card
        AIStatusCard(
            predictedSpeedKmh = aiState.predictedSpeedKmh,
            motionClassification = aiState.motionClassification,
            inferenceTimeMs = aiState.inferenceTimeMs
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "AI Speed Prediction",
                value = String.format("%.1f km/h", aiState.predictedSpeedKmh),
                subtitle = "Confidence: ${aiState.speedConfidencePercentage}%",
                icon = Icons.Default.Memory,
                valueColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Motion Classification",
                value = aiState.motionClassification,
                subtitle = "Confidence: ${aiState.motionConfidencePercentage}%",
                icon = Icons.Default.Psychology,
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Anomaly Detection Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (aiState.anomalyDetected != "None") WarningAmber else AutomotiveCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = "Anomaly", tint = WarningAmber, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "ANOMALY DETECTION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text(text = aiState.anomalyDetected, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                Button(
                    onClick = { viewModel.simulatePothole() },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    shape = CircleShape,
                    modifier = Modifier.shadow(4.dp, shape = CircleShape),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "TEST POTHOLE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // On-Device Model Information Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "ON-DEVICE ML MODEL STATUS", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Model Architecture", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "CNN-LSTM Speed Net", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Inference Engine", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "TensorFlow Lite (NNAPI)", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Execution Target", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "On-Device NPU / GPU", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Model Version", color = TextSecondary, fontSize = 12.sp)
                    Text(text = aiState.modelVersion, color = PurpleAI, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
