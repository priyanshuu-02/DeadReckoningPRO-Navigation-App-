package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateToTechnicalScreen: (String) -> Unit
) {
    var voiceGuidance by remember { mutableStateOf(true) }
    var autoRecenter by remember { mutableStateOf(true) }
    var mapMatching by remember { mutableStateOf(true) }
    var demoMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "SETTINGS & SYSTEM CONFIG", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                Text(text = "Preferences, Technical Diagnostics & Demo Configuration", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical Sub-Navigation Section
        Text(text = "TECHNICAL DIAGNOSTIC SCREENS", color = PurpleAI, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        TechnicalSubNavCard(title = "Sensors & IMU Dashboard", subtitle = "Live 3-axis accel, gyro, mag readings", icon = Icons.Default.Sensors, onClick = { onNavigateToTechnicalScreen("sensors") })
        Spacer(modifier = Modifier.height(6.dp))
        TechnicalSubNavCard(title = "GNSS Monitoring", subtitle = "Satellites view, fix status & HDOP", icon = Icons.Default.SatelliteAlt, onClick = { onNavigateToTechnicalScreen("gnss") })
        Spacer(modifier = Modifier.height(6.dp))
        TechnicalSubNavCard(title = "Dead Reckoning Engine", subtitle = "Vehicle INS coordinates & position drift", icon = Icons.Default.DirectionsCar, onClick = { onNavigateToTechnicalScreen("dead_reckoning") })
        Spacer(modifier = Modifier.height(6.dp))
        TechnicalSubNavCard(title = "Map Matching Engine", subtitle = "Road candidate probabilities & snaps", icon = Icons.Default.AltRoute, onClick = { onNavigateToTechnicalScreen("map_matching") })
        Spacer(modifier = Modifier.height(6.dp))
        TechnicalSubNavCard(title = "Trajectory Comparison", subtitle = "GNSS vs DR vs Map-Matched path overlay", icon = Icons.Default.Polyline, onClick = { onNavigateToTechnicalScreen("trajectory") })
        Spacer(modifier = Modifier.height(6.dp))
        TechnicalSubNavCard(title = "Offline Vector Maps", subtitle = "Map region download management", icon = Icons.Default.Map, onClick = { onNavigateToTechnicalScreen("offline_maps") })
        Spacer(modifier = Modifier.height(6.dp))
        TechnicalSubNavCard(title = "Developer Diagnostics", subtitle = "Latency, FPS, Memory & Frequency telemetry", icon = Icons.Default.BugReport, onClick = { onNavigateToTechnicalScreen("diagnostics") })

        Spacer(modifier = Modifier.height(20.dp))

        // Category 1: NAVIGATION
        Text(text = "NAVIGATION PREFERENCES", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        SettingSwitchRow(title = "Voice Guidance", subtitle = "Audible navigation turn instructions", checked = voiceGuidance, onCheckedChange = { voiceGuidance = it })
        Spacer(modifier = Modifier.height(6.dp))
        SettingSwitchRow(title = "Auto Recenter Map", subtitle = "Keep vehicle centered during active navigation", checked = autoRecenter, onCheckedChange = { autoRecenter = it })

        Spacer(modifier = Modifier.height(16.dp))

        // Category 2: DEAD RECKONING & AI
        Text(text = "DEAD RECKONING & AI ENGINE", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        SettingSwitchRow(title = "Vector Map Matching", subtitle = "Constrain DR positions onto road network", checked = mapMatching, onCheckedChange = { mapMatching = it })
        Spacer(modifier = Modifier.height(6.dp))
        SettingSwitchRow(title = "SIH Demo Mode", subtitle = "Enable floating demo control panel", checked = demoMode, onCheckedChange = { demoMode = it })

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "IDR System Version 2.0-SIH • Build 2026.08", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun TechnicalSubNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Navigate", tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
            )
        }
    }
}
