package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
            Text(text = "SYSTEM PERMISSIONS", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Required Access Setup", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "IDR requires Location, IMU Sensors, and Notification permissions for continuous vehicle navigation.",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            PermissionCard(
                title = "LOCATION",
                subtitle = "Required for GNSS positioning and calibration",
                icon = Icons.Default.LocationOn,
                isGranted = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            PermissionCard(
                title = "SENSORS (IMU)",
                subtitle = "High-sampling rate Accelerometer, Gyroscope & Magnetometer",
                icon = Icons.Default.Sensors,
                isGranted = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            PermissionCard(
                title = "NOTIFICATIONS",
                subtitle = "Foreground service status & GNSS outage alert banners",
                icon = Icons.Default.Notifications,
                isGranted = true
            )
        }

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(8.dp, shape = CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = CircleShape
        ) {
            Text(text = "CONTINUE TO DASHBOARD", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 0.5.sp)
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            if (isGranted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Granted", tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "✓ Granted", color = SuccessGreen, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}
