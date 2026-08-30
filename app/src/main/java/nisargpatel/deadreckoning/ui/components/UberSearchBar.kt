package nisargpatel.deadreckoning.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.domain.model.RouteInfo
import nisargpatel.deadreckoning.ui.theme.*
import org.osmdroid.util.GeoPoint

@Composable
fun UberSearchBar(
    currentRoute: RouteInfo,
    onDestinationSelected: (String, GeoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearchModal by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = UberDarkCard.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, UberCardBorder)
    ) {
        Column(
            modifier = Modifier
                .clickable { showSearchModal = true }
                .padding(14.dp)
        ) {
            // Source Row (Pickup)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // White Square Pickup Marker Icon
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.White, shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentRoute.sourceName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Connecting Vertical Dots
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
                    .width(2.dp)
                    .height(12.dp)
                    .background(UberCardBorder)
            )

            // Destination Row (Drop-off)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mint Green Circle Drop-off Marker Icon
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(UberMintGreen, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentRoute.destinationName,
                        color = UberBlue,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = UberBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Search",
                            color = UberBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showSearchModal) {
        UberDestinationSearchModal(
            onDismiss = { showSearchModal = false },
            onSelect = { name, geoPoint ->
                onDestinationSelected(name, geoPoint)
                showSearchModal = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UberDestinationSearchModal(
    onDismiss: () -> Unit,
    onSelect: (String, GeoPoint) -> Unit
) {
    var destinationName by remember { mutableStateOf("") }
    var coordinates by remember { mutableStateOf("") }
    val coordinateParts = coordinates.split(',').map(String::trim)
    val latitude = coordinateParts.getOrNull(0)?.toDoubleOrNull()
    val longitude = coordinateParts.getOrNull(1)?.toDoubleOrNull()
    val canSelectDestination = latitude != null && longitude != null &&
        latitude in -90.0..90.0 && longitude in -180.0..180.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = UberDarkCard,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "SET DESTINATION",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = destinationName,
                onValueChange = { destinationName = it },
                label = { Text("Destination name") },
                placeholder = { Text("Optional label", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = UberBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UberBlue,
                    unfocusedBorderColor = UberCardBorder,
                    focusedContainerColor = UberCardSurface,
                    unfocusedContainerColor = UberCardSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = coordinates,
                onValueChange = { coordinates = it },
                label = { Text("Destination coordinates") },
                placeholder = { Text("Latitude, longitude", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = UberBlue) },
                isError = coordinates.isNotBlank() && !canSelectDestination,
                supportingText = {
                    if (coordinates.isNotBlank() && !canSelectDestination) {
                        Text("Enter valid latitude and longitude values")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UberBlue,
                    unfocusedBorderColor = UberCardBorder,
                    focusedContainerColor = UberCardSurface,
                    unfocusedContainerColor = UberCardSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSelect(
                        destinationName.ifBlank { "Selected destination" },
                        GeoPoint(requireNotNull(latitude), requireNotNull(longitude))
                    )
                },
                enabled = canSelectDestination,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UberBlue,
                    disabledContainerColor = UberCardBorder,
                    contentColor = Color.White
                )
            ) {
                Text("START ROUTE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
