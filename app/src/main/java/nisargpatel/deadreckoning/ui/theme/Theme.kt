package nisargpatel.deadreckoning.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// SIH transport command color tokens
val UberBlack = Color(0xFF080A0C)
val UberDarkCard = Color(0xFF10151A)
val UberCardSurface = Color(0xFF151B21)
val UberCardBorder = Color(0xFF27313B)
val UberBlue = Color(0xFF38BDF8)
val UberMintGreen = Color(0xFF2DD4BF)
val UberAmber = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFFB7185)

// Shared navigation color tokens
val AutomotiveDarkBg = UberBlack
val AutomotiveSurfaceBg = UberDarkCard
val AutomotiveCardBg = UberCardSurface
val AutomotiveCardBorder = UberCardBorder
val PrimaryBlue = UberBlue
val SuccessGreen = UberMintGreen
val WarningAmber = UberAmber
val PurpleAI = Color(0xFFE879F9)
val RoadInk = Color(0xFF0B1014)
val PanelRaised = Color(0xFF1D252C)
val DividerSoft = Color(0xFF34414D)

// High-contrast text tokens
val TextPrimary = Color(0xFFF7FAFC)
val TextSecondary = Color(0xFFB6C2CC)
val TextMuted = Color(0xFF73808C)

// Navigation geometry shapes
val UberPillShape = CircleShape
val UberCardShape = RoundedCornerShape(8.dp)

private val UberDarkColorScheme = darkColorScheme(
    primary = UberBlue,
    onPrimary = RoadInk,
    secondary = UberMintGreen,
    background = UberBlack,
    surface = UberCardSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

private val IDRTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 30.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

@Composable
fun IDRTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UberDarkColorScheme,
        typography = IDRTypography,
        content = content
    )
}
