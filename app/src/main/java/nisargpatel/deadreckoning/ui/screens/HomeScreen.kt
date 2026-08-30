package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    val gnssState by viewModel.gnssState.collectAsState()
    val aiState by viewModel.aiState.collectAsState()
    val sensorState by viewModel.sensorState.collectAsState()
    val lastSession = sessionState.sessions.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UberBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Uber App Title & System Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "UBER IDR NAV", color = UberBlue, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 1.sp)
                Text(text = "Intelligent Dead Reckoning Navigation", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            ModeIndicator(mode = navState.mode)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Speed & Heading primary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpeedIndicator(speedKmh = navState.speedKmh, modifier = Modifier.weight(1f))
            HeadingIndicator(headingDegrees = navState.headingDegrees, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Uber Destination Quick Search Launcher Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartNavClicked() }
                .shadow(10.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = UberDarkCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, UberBlue.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = UberBlue, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "WHERE TO?", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 17.sp, letterSpacing = 0.5.sp)
                    Text(
                        text = if (navState.latitude == 0.0 && navState.longitude == 0.0) "Tap to select destination & start route" else String.format("Current: %.4f, %.4f", navState.latitude, navState.longitude),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", tint = UberMintGreen, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sensors & Accuracy Summary Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = UberCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, UberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "GNSS ${if (gnssState.isAvailable) "✓" else "WAITING"}",
                        color = if (gnssState.isAvailable) UberMintGreen else UberAmber,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "IMU ${if (sensorState.isAccelAvailable && sensorState.isGyroAvailable) "✓" else "UNAVAILABLE"}",
                        color = if (sensorState.isAccelAvailable && sensorState.isGyroAvailable) UberMintGreen else ErrorRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "AI ${if (aiState.isModelLoaded) "✓" else "UNAVAILABLE"}",
                        color = if (aiState.isModelLoaded) UberMintGreen else ErrorRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Position Accuracy", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (navState.accuracyMeters > 0.0) String.format("%.1f m", navState.accuracyMeters) else "Awaiting fix",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "AI Model Engine", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = aiState.modelVersion,
                            color = if (aiState.isModelLoaded) UberMintGreen else UberBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Start Navigation Button - Uber Capsule Style
        Button(
            onClick = {
                viewModel.startNavigation()
                onStartNavClicked()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(10.dp, shape = CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = UberMintGreen),
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Navigation, contentDescription = "Start Nav", tint = Color.Black, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "START NAVIGATION", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Last Session Card
        if (lastSession != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = UberCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, UberCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = UberBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "LAST SESSION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(text = "${lastSession.distanceKm} km • ${lastSession.durationString}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                    Text(text = lastSession.status, color = UberMintGreen, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
