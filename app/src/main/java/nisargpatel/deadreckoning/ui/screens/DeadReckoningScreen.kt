package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.components.ConfidenceIndicator
import nisargpatel.deadreckoning.ui.components.MetricCard
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.NavigationViewModel

@Composable
fun DeadReckoningScreen(
    viewModel: NavigationViewModel
) {
    val navState by viewModel.navigationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = "DR", tint = WarningAmber, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "DEAD RECKONING ENGINE", color = WarningAmber, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                    Text(text = "Vehicle INS Position Estimation", color = TextSecondary, fontSize = 12.sp)
                }
            }
            ConfidenceIndicator(percentage = navState.confidencePercentage)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Initial GNSS & Estimated DR Position Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "INITIAL GNSS LOCK POSITION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "16.5062, 80.6480", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "CURRENT ESTIMATED POSITION (DR)", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${String.format("%.4f", navState.latitude)}, ${String.format("%.4f", navState.longitude)}",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speed & Heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "DR Vehicle Speed",
                value = String.format("%.1f km/h", navState.speedKmh),
                subtitle = "IMU + AI Fused",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "DR Heading",
                value = String.format("%.0f°", navState.headingDegrees),
                subtitle = "Gyro + Mag Fusion",
                valueColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // DR Duration & Position Drift
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "DR Active Duration",
                value = "${navState.outageDurationSeconds} sec",
                valueColor = WarningAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Estimated Position Drift",
                value = "${navState.accuracyMeters} m",
                subtitle = "Drift Accumulation",
                valueColor = ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dead Reckoning Algorithm Description Note
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "VEHICLE DEAD RECKONING MODEL", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "x_new = x_old + (v_ai × Δt) × sin(heading)\ny_new = y_old + (v_ai × Δt) × cos(heading)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}
