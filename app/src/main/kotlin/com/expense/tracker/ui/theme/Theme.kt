package com.expense.tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    surface = SurfaceLight,
    background = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    surface = SurfaceDark,
    background = SurfaceDark
)

data class PennyTrailColors(
    val profitGreen: Color,
    val lossRed: Color,
    val creditAmber: Color,
    val stockGreen: Color,
    val stockRed: Color,
    val stockAmber: Color,
    val cardGreen: Color,
    val cardRed: Color,
    val cardAmber: Color,
    val cardBlue: Color
)

private val LightPennyTrailColors = PennyTrailColors(
    profitGreen = ProfitGreen,
    lossRed = LossRed,
    creditAmber = CreditAmber,
    stockGreen = StockGreen,
    stockRed = StockRed,
    stockAmber = StockAmber,
    cardGreen = CardGreen,
    cardRed = CardRed,
    cardAmber = CardAmber,
    cardBlue = CardBlue
)

private val DarkPennyTrailColors = PennyTrailColors(
    profitGreen = ProfitGreenLight,
    lossRed = LossRedLight,
    creditAmber = CreditAmberLight,
    stockGreen = StockGreenLight,
    stockRed = StockRedLight,
    stockAmber = StockAmberLight,
    cardGreen = CardGreenDark,
    cardRed = CardRedDark,
    cardAmber = CardAmberDark,
    cardBlue = CardBlueDark
)

val LocalPennyTrailColors = staticCompositionLocalOf { LightPennyTrailColors }

val MaterialTheme.pennyTrailColors: PennyTrailColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPennyTrailColors.current

@Composable
fun PennyTrailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val pennyTrailColors = if (darkTheme) DarkPennyTrailColors else LightPennyTrailColors

    CompositionLocalProvider(LocalPennyTrailColors provides pennyTrailColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
