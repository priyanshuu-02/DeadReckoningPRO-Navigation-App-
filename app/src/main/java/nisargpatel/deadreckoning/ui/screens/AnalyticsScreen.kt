package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.layout.*
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
            title = "Navigation Metrics",
            subtitle = "Trip history, outage recovery, and model error",
            icon = Icons.Default.Analytics
        )

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
            SectionLabel("Recovery analysis")
            DataRow("GNSS reconciliation", "${analyticsState.gnssRecoveryTimeSeconds} sec", SuccessGreen)
            DividerLine()
            DataRow("Heading error bias", "${analyticsState.headingErrorDegrees} deg")
            DataRow("Map matching confidence", "${analyticsState.mapMatchingAccuracyPercentage}%", SuccessGreen)
        }
    }
}
