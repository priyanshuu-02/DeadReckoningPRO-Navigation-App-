package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.components.CommandPanel
import nisargpatel.deadreckoning.ui.components.CommandScreen
import nisargpatel.deadreckoning.ui.components.DataRow
import nisargpatel.deadreckoning.ui.components.DividerLine
import nisargpatel.deadreckoning.ui.components.MetricCard
import nisargpatel.deadreckoning.ui.components.PageHeader
import nisargpatel.deadreckoning.ui.components.SectionLabel
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val analyticsState by viewModel.analyticsState.collectAsState()

    CommandScreen {
        PageHeader(
            title = "Drive insights",
            subtitle = "A clear readout of your navigation resilience",
            icon = Icons.Default.Analytics
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryBlue.copy(alpha = 0.16f),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Route, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("MARK-V trip ledger", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("GNSS, dead-reckoning, and recovery results across saved trips.", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(String.format("%.1f km", analyticsState.totalDistanceKm), color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("recorded", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        SectionLabel("Trip coverage")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Distance",
                value = String.format("%.1f km", analyticsState.totalDistanceKm),
                subtitle = "Trip distance",
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "GNSS Outages",
                value = "${analyticsState.outageCount}",
                subtitle = "Total Outage: ${analyticsState.totalOutageDurationSeconds}s",
                valueColor = WarningAmber,
                modifier = Modifier.weight(1f)
            )
        }

        SectionLabel("Dead-reckoning quality")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Average DR Error",
                value = String.format("%.1f m", analyticsState.averageDriftMeters),
                subtitle = "Mean drift",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Maximum DR Error",
                value = String.format("%.1f m", analyticsState.maxDriftMeters),
                subtitle = "Peak drift",
                valueColor = ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }

        SectionLabel("Prediction and road confidence")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "AI Speed RMSE",
                value = String.format("%.1f km/h", analyticsState.aiSpeedRmseKmh),
                subtitle = "Root mean error",
                valueColor = PurpleAI,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Map Match Accuracy",
                value = "${analyticsState.mapMatchingAccuracyPercentage}%",
                subtitle = "Road snap precision",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        CommandPanel(color = RoadInk, borderColor = DividerSoft) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = SuccessGreen)
                Spacer(Modifier.width(8.dp))
                SectionLabel("Recovery analysis")
            }
            DataRow("GNSS reconciliation", "${analyticsState.gnssRecoveryTimeSeconds} sec", SuccessGreen)
            DividerLine()
            DataRow("Heading error bias", "${analyticsState.headingErrorDegrees} deg")
            DataRow("Map matching confidence", "${analyticsState.mapMatchingAccuracyPercentage}%", SuccessGreen)
        }
    }
}
