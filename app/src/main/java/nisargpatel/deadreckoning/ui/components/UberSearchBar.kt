package nisargpatel.deadreckoning.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
    val presets = listOf(
        Pair("Vijayawada City Center (NH-65)", GeoPoint(16.5062, 80.6480)),
        Pair("Hyderabad Outer Ring Road", GeoPoint(17.3850, 78.4867)),
        Pair("Visakhapatnam Port Highway", GeoPoint(17.6868, 83.2185)),
        Pair("Bengaluru Outer Ring Road", GeoPoint(12.9716, 77.5946)),
        Pair("Chennai Central Highway", GeoPoint(13.0827, 80.2707)),
        Pair("Delhi AIIMS Corridor", GeoPoint(28.5672, 77.2100)),
        Pair("Mumbai Western Express Way", GeoPoint(19.0760, 72.8777))
    )

    var searchQuery by remember { mutableStateOf("") }

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
                text = "WHERE TO?",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search location or address...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = UberBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UberBlue,
                    unfocusedBorderColor = UberCardBorder,
                    focusedContainerColor = UberCardSurface,
                    unfocusedContainerColor = UberCardSurface
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "POPULAR DESTINATIONS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val filteredPresets = if (searchQuery.isBlank()) presets else presets.filter { it.first.contains(searchQuery, ignoreCase = true) }

            filteredPresets.forEach { (name, point) ->
                Surface(
                    onClick = { onSelect(name, point) },
                    shape = RoundedCornerShape(12.dp),
                    color = UberCardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UberCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = UberMintGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = String.format("%.4f, %.4f", point.latitude, point.longitude), color = TextSecondary, fontSize = 11.sp)
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Select", tint = TextSecondary)
                    }
                }
            }
        }
    }
}
