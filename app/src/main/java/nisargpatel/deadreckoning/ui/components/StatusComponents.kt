package nisargpatel.deadreckoning.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.domain.model.NavigationMode
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun ModeIndicator(
    mode: NavigationMode,
    modifier: Modifier = Modifier
) {
    val (color, icon, text) = when (mode) {
        NavigationMode.GNSS_INS -> Triple(SuccessGreen, Icons.Default.GpsFixed, "GNSS + INS")
        NavigationMode.AI_DEAD_RECKONING -> Triple(WarningAmber, Icons.Default.Psychology, "AI DEAD RECKONING")
        NavigationMode.GNSS_RECOVERY -> Triple(PrimaryBlue, Icons.Default.Sync, "GNSS RECOVERY")
        NavigationMode.OFFLINE -> Triple(Color.LightGray, Icons.Default.SignalCellularOff, "OFFLINE NAV")
        NavigationMode.CALIBRATION -> Triple(PurpleAI, Icons.Default.Tune, "CALIBRATION")
        NavigationMode.ERROR -> Triple(ErrorRed, Icons.Default.Warning, "SYSTEM ERROR")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun DemoModeIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = WarningAmber.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(WarningAmber, shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "DEMO MODE",
                color = WarningAmber,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun OutageBanner(
    outageSeconds: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ErrorRed.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GpsOff,
                contentDescription = "Outage Warning",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "⚠ GNSS SIGNAL LOST",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "AI Dead Reckoning Active • Outage: ${outageSeconds}s",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ConfidenceIndicator(
    percentage: Int,
    label: String = "Confidence",
    modifier: Modifier = Modifier
) {
    val color = when {
        percentage >= 90 -> SuccessGreen
        percentage >= 70 -> WarningAmber
        else -> ErrorRed
    }
    val levelText = when {
        percentage >= 90 -> "HIGH"
        percentage >= 70 -> "MEDIUM"
        else -> "LOW"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label: ", color = TextSecondary, fontSize = 12.sp)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.2f)
        ) {
            Text(
                text = "$levelText ($percentage%)",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun HealthIndicator(
    healthPercentage: Int,
    label: String = "System Health",
    modifier: Modifier = Modifier
) {
    val color = when {
        healthPercentage >= 90 -> SuccessGreen
        healthPercentage >= 70 -> WarningAmber
        else -> ErrorRed
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondary, fontSize = 13.sp)
            Text(text = "$healthPercentage%", color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { healthPercentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = AutomotiveCardBorder
        )
    }
}
