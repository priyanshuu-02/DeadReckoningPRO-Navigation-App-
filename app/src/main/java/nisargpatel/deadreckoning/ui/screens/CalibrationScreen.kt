package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.components.HealthIndicator
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun CalibrationScreen(
    onStartNavigation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = "Calibration", tint = PrimaryBlue, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "VEHICLE & SENSOR CALIBRATION", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 0.5.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Verify phone mounting stability, initial heading alignment, and sensor availability prior to departure.",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            HealthIndicator(healthPercentage = 100, label = "Overall Vehicle Readiness")

            Spacer(modifier = Modifier.height(20.dp))

            CalibrationCheckItem(title = "Accelerometer", status = "✓ Ready (100 Hz)")
            CalibrationCheckItem(title = "Gyroscope", status = "✓ Calibrated")
            CalibrationCheckItem(title = "Magnetometer", status = "✓ Calibrated")
            CalibrationCheckItem(title = "Orientation Filter", status = "✓ Aligned")
            CalibrationCheckItem(title = "GNSS Fix", status = "✓ 3D Fix (18 Satellites)")
            CalibrationCheckItem(title = "Vehicle Mount", status = "✓ Stable (Pitch: -1.2°)")
        }

        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = AutomotiveCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "CALIBRATION INSTRUCTIONS", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• Keep phone mounted securely in windshield/dashboard holder.", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(text = "• Avoid magnetic interference from dashboard speakers.", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(text = "• Drive straight for 10 seconds to auto-calibrate heading bias.", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartNavigation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(8.dp, shape = CircleShape),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = CircleShape
            ) {
                Text(text = "START NAVIGATION", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}

@Composable
private fun CalibrationCheckItem(
    title: String,
    status: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(2.dp, shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "OK", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = status, color = SuccessGreen, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}
