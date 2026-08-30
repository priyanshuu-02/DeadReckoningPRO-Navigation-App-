package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.components.*
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartNavClicked: () -> Unit
) {
    val navState by viewModel.navigationState.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val lastSession = sessionState.sessions.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // App Title & System Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "IDR NAVIGATION", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
                Text(text = "Intelligent Dead Reckoning System", color = TextSecondary, fontSize = 12.sp)
            }
            ModeIndicator(mode = navState.mode)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speed & Heading primary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpeedIndicator(speedKmh = navState.speedKmh, modifier = Modifier.weight(1f))
            HeadingIndicator(headingDegrees = navState.headingDegrees, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Map Preview Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Map, contentDescription = "Map Preview", tint = PrimaryBlue.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "LIVE MAP ROUTE PREVIEW", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "NH-65 Highway (Vijayawada)", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sensors & Accuracy Summary Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "GNSS  ✓", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "IMU  ✓", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "AI  ✓", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Position Accuracy", color = TextSecondary, fontSize = 11.sp)
                        Text(text = "${navState.accuracyMeters} m", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "System Health", color = TextSecondary, fontSize = 11.sp)
                        Text(text = "92%", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start Navigation Button
        Button(
            onClick = {
                viewModel.startNavigation()
                onStartNavClicked()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Nav", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "START NAVIGATION", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Last Session Card
        if (lastSession != null) {
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
                    Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "LAST SESSION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "${lastSession.distanceKm} km • ${lastSession.durationString}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text(text = lastSession.status, color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
