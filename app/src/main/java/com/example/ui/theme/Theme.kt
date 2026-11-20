package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Immutable
data class SolomonExtraColors(
    val gold: Color,
    val goldBright: Color,
    val goldDeep: Color,
    val cardElevated: Color,
    val subtleBorder: Color,
    val emerald: Color,
    val sapphire: Color,
    val amethyst: Color,
    val coral: Color,
    val amber: Color
)

private val DarkExtraColors = SolomonExtraColors(
    gold = GoldPrimary,
    goldBright = GoldBright,
    goldDeep = GoldDeep,
    cardElevated = ObsidianElevated,
    subtleBorder = GoldBorderSubtle,
    emerald = EmeraldAccent,
    sapphire = SapphireAccent,
    amethyst = AmethystAccent,
    coral = CoralAccent,
    amber = AmberAccent
)

private val LightExtraColors = SolomonExtraColors(
    gold = RoyalBronze,
    goldBright = GoldPrimary,
    goldDeep = Color(0xFF704C09),
    cardElevated = Color(0xFFEDE2CB),
    subtleBorder = Color(0x339A6B13),
    emerald = EmeraldDark,
    sapphire = Color(0xFF2563EB),
    amethyst = Color(0xFF7C3AED),
    coral = Color(0xFFE11D48),
    amber = Color(0xFFD97706)
)

val LocalSolomonColors = staticCompositionLocalOf { DarkExtraColors }

private val SolomonDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = ObsidianDeep,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = GoldBright,
    secondary = EmeraldAccent,
    onSecondary = ObsidianDeep,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldAccent,
    tertiary = AmethystAccent,
    onTertiary = ObsidianDeep,
    background = ObsidianDeep,
    onBackground = IvoryText,
    surface = ObsidianSurface,
    onSurface = IvoryText,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = MutedSlateText,
    outline = GoldBorderSubtle,
    error = CoralAccent,
    onError = ObsidianDeep
)

private val SolomonLightColorScheme = lightColorScheme(
    primary = RoyalBronze,
    onPrimary = Color.White,
    primaryContainer = RoyalBronzeContainer,
    onPrimaryContainer = Color(0xFF473004),
    secondary = EmeraldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF0F3532),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color.White,
    background = ParchmentBg,
    onBackground = InkDark,
    surface = ParchmentSurface,
    onSurface = InkDark,
    surfaceVariant = ParchmentCard,
    onSurfaceVariant = InkMuted,
    outline = Color(0x339A6B13),
    error = Color(0xFFBE123C),
    onError = Color.White
)

@Composable
fun SolomonsRingTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SolomonDarkColorScheme else SolomonLightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(
        LocalSolomonColors provides extraColors,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SolomonsRingTypography,
            content = content
        )
    }
}
