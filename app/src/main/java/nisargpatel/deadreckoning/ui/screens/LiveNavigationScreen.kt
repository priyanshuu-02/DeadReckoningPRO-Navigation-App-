package nisargpatel.deadreckoning.ui.screens

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
import androidx.compose.ui.viewinterop.AndroidView
import nisargpatel.deadreckoning.domain.model.NavigationMode
import nisargpatel.deadreckoning.ui.components.*
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.NavigationViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

@Composable
fun LiveNavigationScreen(
    viewModel: NavigationViewModel
) {
    val navState by viewModel.navigationState.collectAsState()
    val gnssState by viewModel.gnssState.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
    ) {
        // 1. Dominant OSMDroid MapView Area (wrapped with AndroidView)
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    val rotationOverlay = RotationGestureOverlay(context, this)
                    rotationOverlay.isEnabled = true
                    overlays.add(rotationOverlay)
                    controller.setZoom(18.0)
                    controller.setCenter(GeoPoint(16.5062, 80.6480))
                }
            },
            update = { mapView ->
                val targetPoint = navState.latitude.let { lat ->
                    if (lat != 0.0) GeoPoint(lat, navState.longitude) else GeoPoint(16.5062, 80.6480)
                }
                mapView.controller.setCenter(targetPoint)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Status HUD Overlay (Navigation Mode & Outage Warning)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeIndicator(mode = navState.mode)
                ConfidenceIndicator(percentage = navState.confidencePercentage)
            }

            if (navState.mode == NavigationMode.AI_DEAD_RECKONING || navState.outageDurationSeconds > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                OutageBanner(outageSeconds = navState.outageDurationSeconds)
            }
        }

        // Bottom Automotive Dashboard Overlay (Speed, Heading, Controls)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Floating SIH Demo Controls Panel
            DemoControls(
                onGNSSActive = { viewModel.simulateGNSSActive() },
                onSimulateOutage = { viewModel.simulateOutage() },
                onSimulatePothole = { viewModel.simulatePothole() },
                onSimulateRecovery = { viewModel.simulateRecovery() },
                onSimulateOffline = { viewModel.simulateOffline() },
                onSimulateError = { viewModel.simulateError() },
                onResetDemo = { viewModel.resetDemo() },
                onAutoPlay = { viewModel.startAutoPlay() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Automotive Bottom HUD Card - Glassmorphism & Shadow
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = AutomotiveDarkBg.copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "SPEED", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(
                            text = String.format("%.1f km/h", navState.speedKmh),
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Text(text = "HEADING", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(
                            text = String.format("%.0f°", navState.headingDegrees),
                            color = PrimaryBlue,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Text(text = "ACCURACY", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(
                            text = "${navState.accuracyMeters} m",
                            color = if (navState.accuracyMeters < 5) SuccessGreen else WarningAmber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Start/Stop Navigation iOS Pill Button
                    IconButton(
                        onClick = {
                            if (navState.isNavigating) viewModel.stopNavigation() else viewModel.startNavigation()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(6.dp, shape = CircleShape)
                            .background(
                                color = if (navState.isNavigating) ErrorRed else SuccessGreen,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (navState.isNavigating) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Nav",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
