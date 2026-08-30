package nisargpatel.deadreckoning.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Deep Luxury Automotive & Aerospace Color Palette
val AutomotiveDarkBg = Color(0xFF070B14)
val AutomotiveSurfaceBg = Color(0xFF0F172A)
val AutomotiveCardBg = Color(0xFF162036)
val AutomotiveCardBorder = Color(0xFF283859)
val AutomotiveGlassHighlight = Color(0xFF1F2D4A)

// High-Contrast Neon Accent Tokens
val PrimaryBlue = Color(0xFF00E5FF)       // Electric Cyan / Azure
val PrimaryBlueDark = Color(0xFF0284C7)   // Deep Cyan
val SuccessGreen = Color(0xFF10B981)      // Emerald Green
val WarningAmber = Color(0xFFF59E0B)      // Radiant Gold / Amber
val ErrorRed = Color(0xFFEF4444)          // Coral Red
val PurpleAI = Color(0xFFA855F7)          // Quantum Violet

// High Contrast Text Tokens
val TextPrimary = Color(0xFFFFFFFF)       // Pure White
val TextSecondary = Color(0xFF94A3B8)     // Silver Slate
val TextMuted = Color(0xFF64748B)         // Dimmed Slate

// Reusable iOS-Style Pill & Glass Extensions
val IosPillShape = CircleShape
val IosCardShape = RoundedCornerShape(20.dp)

private val PremiumDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.Black,
    secondary = PurpleAI,
    background = AutomotiveDarkBg,
    surface = AutomotiveCardBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

@Composable
fun IDRTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        typography = Typography(),
        content = content
    )
}
