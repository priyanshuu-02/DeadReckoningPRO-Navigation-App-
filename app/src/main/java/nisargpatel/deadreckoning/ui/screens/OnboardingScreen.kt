package nisargpatel.deadreckoning.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import nisargpatel.deadreckoning.ui.theme.*

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            title = "Navigate Beyond GPS",
            description = "The Intelligent Dead Reckoning system automatically continues vehicle navigation when GNSS signal is lost in tunnels, urban canyons, or dense cover.",
            icon = Icons.Default.GpsOff
        ),
        OnboardingPageData(
            title = "AI-Powered Motion Intelligence",
            description = "On-device AI algorithms analyze accelerometer and gyroscope IMU sensor data in real-time to estimate vehicle speed and detect road anomalies.",
            icon = Icons.Default.Psychology
        ),
        OnboardingPageData(
            title = "Seamless GNSS Recovery",
            description = "When GNSS returns, the system smoothly reconciles dead-reckoning trajectory with GPS coordinates, eliminating position drift.",
            icon = Icons.Default.Sync
        ),
        OnboardingPageData(
            title = "Offline-Ready Map Matching",
            description = "Integrated vector map matching aligns estimated positions directly onto road networks without requiring cloud connectivity.",
            icon = Icons.Default.Map
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutomotiveDarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip Button Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onOnboardingFinished) {
                Text(text = "SKIP", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val data = pages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = AutomotiveCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AutomotiveCardBorder),
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = data.title,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = data.title,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = data.description,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        // Page Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (active) 24.dp else 8.dp, 8.dp)
                        .background(
                            color = if (active) PrimaryBlue else AutomotiveCardBorder,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "BACK", color = TextPrimary)
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onOnboardingFinished()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "GET STARTED" else "NEXT",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
