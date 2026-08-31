package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.domain.state.NavigationSession
import nisargpatel.deadreckoning.ui.components.CommandPanel
import nisargpatel.deadreckoning.ui.components.DataRow
import nisargpatel.deadreckoning.ui.components.DividerLine
import nisargpatel.deadreckoning.ui.components.MetricCard
import nisargpatel.deadreckoning.ui.components.SectionLabel
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun SessionDetailScreen(
    session: NavigationSession,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = "Trip Detail", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 21.sp)
                Text(text = session.dateString, color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session Overview Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Distance",
                value = "${session.distanceKm} km",
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Duration",
                value = session.durationString,
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "GNSS Outages",
                value = "${session.outageCount} Outages",
                valueColor = WarningAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "DR Active Time",
                value = "${session.drDurationSeconds}s",
                valueColor = WarningAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The exact trip paths are stored at stop time and remain available here.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(8.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            StoredTrajectoryPreview(session)
        }

        Spacer(modifier = Modifier.height(16.dp))

        CommandPanel(color = RoadInk, borderColor = DividerSoft) {
            SectionLabel("Error summary")
            DataRow("Max error", "${session.maxErrorMeters} m", WarningAmber)
            DividerLine()
            DataRow("Average error", "${session.avgErrorMeters} m")
            DataRow("Status", session.status, SuccessGreen)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export Session Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = "Export GPX", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Export GPX", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = "Export CSV", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Export CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StoredTrajectoryPreview(session: NavigationSession) {
    val allPoints = session.plannedRoute + session.gnssPath + session.deadReckoningPath
    if (allPoints.size < 2) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Map, contentDescription = "Session map", tint = PrimaryBlue.copy(alpha = 0.3f), modifier = Modifier.size(44.dp))
                Spacer(Modifier.height(6.dp))
                Text("No path points recorded for this trip.", color = TextSecondary, fontSize = 12.sp)
            }
        }
        return
    }
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        val minLat = allPoints.minOf { it.latitude }
        val maxLat = allPoints.maxOf { it.latitude }
        val minLon = allPoints.minOf { it.longitude }
        val maxLon = allPoints.maxOf { it.longitude }
        fun pointFor(latitude: Double, longitude: Double): Offset = Offset(
            if (maxLon == minLon) size.width / 2f else ((longitude - minLon) / (maxLon - minLon) * size.width).toFloat(),
            if (maxLat == minLat) size.height / 2f else (size.height - (latitude - minLat) / (maxLat - minLat) * size.height).toFloat()
        )
        fun drawPathFor(points: List<nisargpatel.deadreckoning.domain.state.TrajectoryPoint>, color: Color, width: Float) {
            if (points.size < 2) return
            val path = Path().apply {
                moveTo(pointFor(points.first().latitude, points.first().longitude).x, pointFor(points.first().latitude, points.first().longitude).y)
                points.drop(1).forEach { point ->
                    pointFor(point.latitude, point.longitude).let { lineTo(it.x, it.y) }
                }
            }
            drawPath(path, color, style = Stroke(width = width))
        }
        drawPathFor(session.plannedRoute, PrimaryBlue, 4f)
        drawPathFor(session.gnssPath, SuccessGreen, 5f)
        drawPathFor(session.deadReckoningPath, ErrorRed, 5f)
    }
}
