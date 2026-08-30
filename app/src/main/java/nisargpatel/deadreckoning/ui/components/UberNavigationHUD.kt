package nisargpatel.deadreckoning.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.domain.model.ManeuverIconType
import nisargpatel.deadreckoning.domain.model.RouteInfo
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun UberNavigationHUD(
    routeInfo: RouteInfo,
    speedKmh: Double,
    headingDegrees: Double,
    accuracyMeters: Double,
    isNavigating: Boolean,
    onToggleNavigation: () -> Unit,
    onRecenterMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = UberDarkCard.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, UberCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Turn-by-Turn Instruction Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Maneuver Direction Arrow Badge
                Surface(
                    shape = CircleShape,
                    color = UberMintGreen,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when (routeInfo.maneuverIconType) {
                            ManeuverIconType.LEFT -> Icons.Default.TurnLeft
                            ManeuverIconType.RIGHT -> Icons.Default.TurnRight
                            ManeuverIconType.SLIGHT_LEFT -> Icons.Default.TurnSlightLeft
                            ManeuverIconType.SLIGHT_RIGHT -> Icons.Default.TurnSlightRight
                            ManeuverIconType.UTURN -> Icons.AutoMirrored.Filled.Undo
                            ManeuverIconType.ARRIVED -> Icons.Default.CheckCircle
                            ManeuverIconType.STRAIGHT -> Icons.Default.ArrowUpward
                        }
                        Icon(imageVector = icon, contentDescription = "Maneuver", tint = Color.Black, modifier = Modifier.size(26.dp))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routeInfo.nextManeuver,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Destination: ${routeInfo.destinationName}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = UberCardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Trip Progress Telemetry Grid (ETA min, Remaining km, Speed km/h, Heading °)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ESTIMATED ETA", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text(
                        text = "${routeInfo.estimatedTimeMinutes} min",
                        color = UberMintGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Text(text = "DISTANCE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text(
                        text = "${routeInfo.totalDistanceKm} km",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Text(text = "SPEED", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text(
                        text = String.format("%.1f", speedKmh),
                        color = UberBlue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Text(text = "HEADING", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text(
                        text = String.format("%.0f°", headingDegrees),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Pill Buttons Row (Recenter & Start/End Navigation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Recenter Map Button
                OutlinedButton(
                    onClick = onRecenterMap,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, UberCardBorder),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Recenter", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "RECENTER", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }

                // Start/End Route Navigation Capsule Button
                Button(
                    onClick = onToggleNavigation,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(46.dp)
                        .shadow(6.dp, shape = CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNavigating) ErrorRed else UberMintGreen
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isNavigating) Icons.Default.Stop else Icons.Default.Navigation,
                        contentDescription = "Navigate",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isNavigating) "END TRIP" else "START ROUTE",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
