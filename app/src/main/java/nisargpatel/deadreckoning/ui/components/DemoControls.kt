package nisargpatel.deadreckoning.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun DemoControls(
    onGNSSActive: () -> Unit,
    onSimulateOutage: () -> Unit,
    onSimulatePothole: () -> Unit,
    onSimulateRecovery: () -> Unit,
    onSimulateOffline: () -> Unit,
    onSimulateError: () -> Unit,
    onResetDemo: () -> Unit,
    onAutoPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = AutomotiveDarkBg.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningAmber.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DemoModeIndicator()
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "SIH Demo Controller", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Demo Panel",
                        tint = WarningAmber
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                // iOS Pill Action Buttons (Auto Play & Reset Demo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAutoPlay,
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, shape = CircleShape),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAI),
                        shape = CircleShape,
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Auto Play", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "AUTO PLAY", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = onResetDemo,
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, shape = CircleShape),
                        colors = ButtonDefaults.buttonColors(containerColor = AutomotiveCardBorder),
                        shape = CircleShape,
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Demo", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "RESET DEMO", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode trigger iOS Pill Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onGNSSActive,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(text = "GNSS ACTIVE", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = onSimulateOutage,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(text = "OUTAGE", fontSize = 10.sp, color = ErrorRed, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = onSimulatePothole,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(text = "POTHOLE", fontSize = 10.sp, color = WarningAmber, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mode trigger iOS Pill Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onSimulateRecovery,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(text = "RECOVERY", fontSize = 10.sp, color = PrimaryBlue, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = onSimulateOffline,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(text = "OFFLINE", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = onSimulateError,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(text = "ERROR", fontSize = 10.sp, color = ErrorRed, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
