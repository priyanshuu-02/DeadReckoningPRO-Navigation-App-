package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.components.MetricCard
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.GNSSViewModel

@Composable
fun GNSSScreen(
    viewModel: GNSSViewModel
) {
    val gnssState by viewModel.gnssState.collectAsState()

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
                Icon(
                    imageVector = if (gnssState.isAvailable) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                    contentDescription = "GNSS",
                    tint = if (gnssState.isAvailable) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "GNSS MONITORING", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                    Text(text = "GPS / GLONASS Satellite Telemetry", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = (if (gnssState.isAvailable) SuccessGreen else ErrorRed).copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (gnssState.isAvailable) "ACTIVE" else "LOST",
                    color = if (gnssState.isAvailable) SuccessGreen else ErrorRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Satellite & Fix Overview Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SatelliteAlt, contentDescription = "Sats", tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${gnssState.satelliteCount} Satellites In View", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(text = gnssState.fixStatus, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Accuracy", color = TextSecondary, fontSize = 11.sp)
                        Text(text = "${gnssState.accuracyMeters} m", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column {
                        Text(text = "Signal Quality", color = TextSecondary, fontSize = 11.sp)
                        Text(text = "${gnssState.signalQualityPercentage}%", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Outage Duration", color = TextSecondary, fontSize = 11.sp)
                        Text(text = "${gnssState.outageDurationSeconds}s", color = if (gnssState.outageDurationSeconds > 0) ErrorRed else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Position & Coordinates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Latitude",
                value = String.format("%.4f", gnssState.latitude),
                subtitle = "WGS84 Datum",
                valueColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Longitude",
                value = String.format("%.4f", gnssState.longitude),
                subtitle = "WGS84 Datum",
                valueColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Speed & Bearing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "GNSS Speed",
                value = String.format("%.1f km/h", gnssState.speedKmh),
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "GNSS Bearing",
                value = String.format("%.0f°", gnssState.bearingDegrees),
                valueColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // HDOP / VDOP Dilution of Precision
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Horizontal DOP (HDOP)", color = TextSecondary, fontSize = 11.sp)
                    Text(text = String.format("%.1f", gnssState.hdop), color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Vertical DOP (VDOP)", color = TextSecondary, fontSize = 11.sp)
                    Text(text = String.format("%.1f", gnssState.vdop), color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
