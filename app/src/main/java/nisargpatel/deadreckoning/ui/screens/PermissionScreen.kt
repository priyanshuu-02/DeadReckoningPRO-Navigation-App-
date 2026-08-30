package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun PermissionScreen(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "SYSTEM PERMISSIONS", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Required Access Setup", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "IDR requires Location, IMU Sensors, and Notification permissions for continuous vehicle navigation.",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            PermissionCard(
                title = "LOCATION",
                subtitle = "Required for GNSS positioning and calibration",
                icon = Icons.Default.LocationOn,
                isGranted = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                title = "SENSORS (IMU)",
                subtitle = "High-sampling rate Accelerometer, Gyroscope & Magnetometer",
                icon = Icons.Default.Sensors,
                isGranted = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                title = "NOTIFICATIONS",
                subtitle = "Foreground service status & GNSS outage alert banners",
                icon = Icons.Default.Notifications,
                isGranted = true
            )
        }

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "CONTINUE TO DASHBOARD", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isGranted: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            if (isGranted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Granted", tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "✓ Granted", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
