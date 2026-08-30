package nisargpatel.deadreckoning.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AutomotiveDarkBg = Color(0xFF0F172A)
val AutomotiveCardBg = Color(0xFF1E293B)
val AutomotiveCardBorder = Color(0xFF334155)

val PrimaryBlue = Color(0xFF38BDF8)
val SuccessGreen = Color(0xFF4ADE80)
val WarningAmber = Color(0xFFFBBF24)
val ErrorRed = Color(0xFFF87171)
val PurpleAI = Color(0xFFA78BFA)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
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
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
