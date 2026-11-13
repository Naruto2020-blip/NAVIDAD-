package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val NochebuenaColorScheme = darkColorScheme(
    primary = CandlelightGold,
    onPrimary = MidnightEvergreen,
    primaryContainer = PineElevatedSurface,
    onPrimaryContainer = WarmAmber,
    secondary = CranberryRed,
    onSecondary = FrostWhite,
    secondaryContainer = VelvetCrimson,
    onSecondaryContainer = FrostWhite,
    tertiary = WarmAmber,
    onTertiary = MidnightEvergreen,
    background = MidnightEvergreen,
    onBackground = FrostWhite,
    surface = DeepPineSurface,
    onSurface = FrostWhite,
    surfaceVariant = PineCardSurface,
    onSurfaceVariant = MistSage,
    outline = PineOutline,
    outlineVariant = PineElevatedSurface,
)

private val BlancaNavidadColorScheme = lightColorScheme(
    primary = FestiveBerryRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD9),
    onPrimaryContainer = Color(0xFF410008),
    secondary = WarmGarlandGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8EBD9),
    onSecondaryContainer = Color(0xFF062920),
    tertiary = AntiqueGold,
    onTertiary = Color.White,
    background = SnowCreamBackground,
    onBackground = CharcoalPine,
    surface = SnowSurface,
    onSurface = CharcoalPine,
    surfaceVariant = SoftHollySurface,
    onSurfaceVariant = SlatePine,
    outline = WarmOutline,
    outlineVariant = Color(0xFFEBE4D8),
)

val NavidadShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun NavidadTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NochebuenaColorScheme else BlancaNavidadColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NavidadTypography,
        shapes = NavidadShapes,
        content = content,
    )
}
