package nisargpatel.deadreckoning.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AutomotiveDarkBg.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DemoModeIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "SIH Demo Controller", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onAutoPlay,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAI),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Auto Play", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "AUTO PLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onResetDemo,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AutomotiveCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Demo", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "RESET DEMO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mode triggers row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = onGNSSActive,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(text = "GNSS ACTIVE", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onSimulateOutage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(text = "OUTAGE", fontSize = 10.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onSimulatePothole,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(text = "POTHOLE", fontSize = 10.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Mode triggers row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = onSimulateRecovery,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(text = "RECOVERY", fontSize = 10.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onSimulateOffline,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(text = "OFFLINE", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onSimulateError,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(text = "ERROR", fontSize = 10.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
