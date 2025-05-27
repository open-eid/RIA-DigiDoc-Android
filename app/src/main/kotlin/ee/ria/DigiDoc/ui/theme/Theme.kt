@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import ee.ria.DigiDoc.ui.theme.Dimensions.MCornerRadius
import ee.ria.DigiDoc.ui.theme.Dimensions.MSCornerRadius

val buttonRoundedCornerShape = RoundedCornerShape(MSCornerRadius)
val buttonRoundCornerShape = RoundedCornerShape(MCornerRadius)

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        primaryContainer = DarkPrimaryContainer,
        onPrimaryContainer = DarkOnPrimaryContainer,
        secondary = DarkSecondaryContainer,
        onSecondary = DarkOnSecondaryContainer,
        secondaryContainer = DarkSecondaryContainer,
        onSecondaryContainer = DarkOnSecondaryContainer,
        tertiary = DarkTertiary,
        onTertiary = DarkOnTertiary,
        tertiaryContainer = DarkTertiaryContainer,
        onTertiaryContainer = DarkOnTertiaryContainer,
        error = DarkError,
        onError = DarkOnError,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkOnErrorContainer,
        background = Black,
        onBackground = White,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurface = DarkOnSurface,
        onSurfaceVariant = DarkOnSurfaceVariant,
        surfaceContainer = DarkSurfaceContainer,
        surfaceContainerLow = DarkSurfaceContainerLow,
        surfaceContainerHigh = DarkSurfaceContainerHigh,
        surfaceContainerHighest = DarkSurfaceContainerHighest,
        inversePrimary = DarkInversePrimary,
        inverseSurface = DarkInverseSurface,
        inverseOnSurface = DarkInverseOnSurface,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        primaryContainer = LightPrimaryContainer,
        onPrimaryContainer = LightOnPrimaryContainer,
        secondary = LightSecondaryContainer,
        onSecondary = LightOnSecondaryContainer,
        secondaryContainer = LightSecondaryContainer,
        onSecondaryContainer = LightOnSecondaryContainer,
        tertiary = LightTertiary,
        onTertiary = LightOnTertiary,
        tertiaryContainer = LightTertiaryContainer,
        onTertiaryContainer = LightOnTertiaryContainer,
        error = LightError,
        onError = LightOnError,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightOnErrorContainer,
        background = White,
        onBackground = Black,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
        surface = LightSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurface = LightOnSurface,
        onSurfaceVariant = LightOnSurfaceVariant,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerLow = LightSurfaceContainerLow,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        surfaceContainerHighest = LightSurfaceContainerHighest,
        inversePrimary = LightInversePrimary,
        inverseSurface = LightInverseSurface,
        inverseOnSurface = LightInverseOnSurface,
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RIADigiDocTheme(
    darkTheme: Boolean? = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = darkTheme ?: isSystemInDarkTheme()
    val colorScheme =
        when {
            dynamicColor -> {
                val context = LocalContext.current
                if (useDarkTheme == true) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            useDarkTheme == true -> DarkColorScheme
            else -> LightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val color = colorScheme.surface.toArgb()
            window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
                view.setBackgroundColor(color)
                windowInsets
            }
            darkTheme?.let { WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !it }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(),
        content = content,
    )
}
