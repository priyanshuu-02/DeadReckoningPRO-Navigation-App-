package nisargpatel.deadreckoning.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Signature Uber Automotive & Navigation Color Tokens
val UberBlack = Color(0xFF000000)          // Pure Uber Black Background
val UberDarkCard = Color(0xFF121212)       // Elevated Uber Surface Card
val UberCardSurface = Color(0xFF1A1A1A)    // Secondary Card Surface
val UberCardBorder = Color(0xFF2C2C2C)     // High-Contrast Sleek Border
val UberBlue = Color(0xFF276EF1)           // Uber Electric Blue Accent
val UberMintGreen = Color(0xFF10B981)      // Mint Green Route & Success Token
val UberAmber = Color(0xFFFFC043)          // Gold / Amber Warning Token
val ErrorRed = Color(0xFFEF4444)           // Coral Red

// Legacy Compatibility Accent Tokens mapped to Uber Palette
val AutomotiveDarkBg = UberBlack
val AutomotiveSurfaceBg = UberDarkCard
val AutomotiveCardBg = UberCardSurface
val AutomotiveCardBorder = UberCardBorder
val PrimaryBlue = UberBlue
val SuccessGreen = UberMintGreen
val WarningAmber = UberAmber
val PurpleAI = Color(0xFFA855F7)

// High-Contrast Text Tokens
val TextPrimary = Color(0xFFFFFFFF)        // Pure Crisp White
val TextSecondary = Color(0xFFA0A0A0)      // Silver Secondary Slate
val TextMuted = Color(0xFF666666)          // Muted Charcoal

// Uber Geometry Shapes
val UberPillShape = CircleShape
val UberCardShape = RoundedCornerShape(16.dp)

private val UberDarkColorScheme = darkColorScheme(
    primary = UberBlue,
    onPrimary = Color.White,
    secondary = UberMintGreen,
    background = UberBlack,
    surface = UberCardSurface,
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
        colorScheme = UberDarkColorScheme,
        typography = Typography(),
        content = content
    )
}
