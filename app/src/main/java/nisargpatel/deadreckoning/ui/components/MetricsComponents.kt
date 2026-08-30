package nisargpatel.deadreckoning.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun SpeedIndicator(
    speedKmh: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Speed",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "SPEED", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%.1f", speedKmh),
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "km/h", color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun HeadingIndicator(
    headingDegrees: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "Heading",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "HEADING", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%.0f°", headingDegrees),
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Degrees", color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = title, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(text = title.uppercase(), color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AIStatusCard(
    predictedSpeedKmh: Double,
    motionClassification: String,
    inferenceTimeMs: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleAI.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI", tint = PurpleAI)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ON-DEVICE AI INTELLIGENCE", color = PurpleAI, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PurpleAI.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${inferenceTimeMs}ms latency",
                        color = PurpleAI,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "AI Predicted Speed", color = TextSecondary, fontSize = 11.sp)
                    Text(text = String.format("%.1f km/h", predictedSpeedKmh), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Motion Classification", color = TextSecondary, fontSize = 11.sp)
                    Text(text = motionClassification, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
