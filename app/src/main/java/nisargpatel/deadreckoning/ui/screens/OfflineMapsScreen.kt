package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nisargpatel.deadreckoning.ui.theme.*

@Composable
fun OfflineMapsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Map, contentDescription = "Offline Maps", tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "OFFLINE MAP MANAGEMENT", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                Text(text = "Vector Map Region Packages for Offline Dead Reckoning", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Storage & Map Version Summary Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AutomotiveCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = "Storage", tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Storage Used", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text(text = "1.2 GB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Vector Map Version", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "v2026.08 (OSM)", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "OFFLINE VECTOR MAP REGIONS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        RegionCard(name = "Andhra Pradesh (Full)", size = "420 MB", isDownloaded = true)
        Spacer(modifier = Modifier.height(8.dp))
        RegionCard(name = "Karnataka (Full)", size = "580 MB", isDownloaded = true)
        Spacer(modifier = Modifier.height(8.dp))
        RegionCard(name = "Telangana (Full)", size = "310 MB", isDownloaded = true)
        Spacer(modifier = Modifier.height(8.dp))
        RegionCard(name = "Tamil Nadu (Full)", size = "640 MB", isDownloaded = false)
        Spacer(modifier = Modifier.height(8.dp))
        RegionCard(name = "Maharashtra (Full)", size = "890 MB", isDownloaded = false)
    }
}

@Composable
private fun RegionCard(
    name: String,
    size: String,
    isDownloaded: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AutomotiveCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = size, color = TextSecondary, fontSize = 12.sp)
            }

            if (isDownloaded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Downloaded", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "✓ Available", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Download", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
