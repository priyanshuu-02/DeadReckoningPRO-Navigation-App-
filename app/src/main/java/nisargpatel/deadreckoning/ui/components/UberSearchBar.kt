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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import nisargpatel.deadreckoning.domain.model.RouteInfo
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.util.PlaceSearchResult
import org.osmdroid.util.GeoPoint

@Composable
fun UberSearchBar(
    currentRoute: RouteInfo,
    onDestinationSelected: (String, GeoPoint) -> Unit,
    onSearch: suspend (String) -> List<PlaceSearchResult>,
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
            },
            onSearch = onSearch
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UberDestinationSearchModal(
    onDismiss: () -> Unit,
    onSelect: (String, GeoPoint) -> Unit,
    onSearch: suspend (String) -> List<PlaceSearchResult>
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PlaceSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun searchPlaces() {
        val searchQuery = query.trim()
        if (searchQuery.length < 3) {
            results = emptyList()
            hasSearched = false
            return
        }
        isSearching = true
        hasSearched = true
        searchError = null
        val outcome = runCatching { onSearch(searchQuery) }
        // Ignore an older response when the user has continued typing.
        if (query.trim() != searchQuery) return
        results = outcome.getOrElse {
            searchError = "Could not find places. Check your connection and try again."
            emptyList()
        }
        isSearching = false
    }

    // Mirrors map-app autocomplete: wait briefly for the user to pause typing,
    // then refresh suggestions without requiring a separate search action.
    LaunchedEffect(query) {
        results = emptyList()
        searchError = null
        if (query.trim().length >= 3) {
            delay(450)
            searchPlaces()
        } else {
            hasSearched = false
        }
    }

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
                text = "SEARCH DESTINATION",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    searchError = null
                },
                label = { Text("Where do you want to go?") },
                placeholder = { Text("Search a place, landmark, or address", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = UberBlue) },
                trailingIcon = {
                    if (isSearching) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else IconButton(onClick = { scope.launch { searchPlaces() } }, enabled = query.trim().length >= 3) {
                        Icon(Icons.Default.Search, contentDescription = "Search places", tint = UberBlue)
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
            Spacer(modifier = Modifier.height(8.dp))
            Text("Choose a result to fetch its map coordinates and create the route.", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { scope.launch { searchPlaces() } },
                enabled = query.trim().length >= 3 && !isSearching,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UberBlue)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isSearching) "SEARCHING…" else "SEARCH MAP", fontWeight = FontWeight.Bold)
            }
            searchError?.let { Text(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
            if (results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                results.forEach { result ->
                    Surface(
                        onClick = { onSelect(result.displayName, GeoPoint(result.latitude, result.longitude)) },
                        color = UberCardSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = UberMintGreen)
                            Spacer(Modifier.width(10.dp))
                            Text(result.displayName, color = TextPrimary, fontSize = 13.sp, maxLines = 2, modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else if (!isSearching && hasSearched) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("No places found. Try a more specific name or address.", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}
