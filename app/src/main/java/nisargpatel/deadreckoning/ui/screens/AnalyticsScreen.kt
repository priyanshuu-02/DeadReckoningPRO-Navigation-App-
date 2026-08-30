package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.components.MetricCard
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val analyticsState by viewModel.analyticsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Analytics, contentDescription = "Analytics", tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "NAVIGATION ANALYTICS", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                Text(text = "System Performance & Error Metric Evaluation", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Distance & Outage Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Distance",
                value = String.format("%.1f km", analyticsState.totalDistanceKm),
                subtitle = "Total Trip Covered",
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "GNSS Outages",
                value = "${analyticsState.outageCount} Outages",
                subtitle = "Total Outage: ${analyticsState.totalOutageDurationSeconds}s",
                valueColor = WarningAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Drift & Position Error Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Average DR Error",
                value = String.format("%.1f m", analyticsState.averageDriftMeters),
                subtitle = "Mean Position Drift",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Maximum DR Error",
                value = String.format("%.1f m", analyticsState.maxDriftMeters),
                subtitle = "Peak Outage Drift",
                valueColor = ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Speed RMSE & Map Matching Accuracy Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "AI Speed RMSE",
                value = String.format("%.1f km/h", analyticsState.aiSpeedRmseKmh),
                subtitle = "Root Mean Sq Error",
                valueColor = PurpleAI,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Map Match Accuracy",
                value = "${analyticsState.mapMatchingAccuracyPercentage}%",
                subtitle = "Road Snap Precision",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GNSS Recovery Time Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "GNSS RECOVERY LATENCY", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Reconciliation Latency", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "${analyticsState.gnssRecoveryTimeSeconds} sec", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Heading Error Bias", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "${analyticsState.headingErrorDegrees}°", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
