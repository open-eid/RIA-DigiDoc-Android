@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
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
import ee.ria.DigiDoc.ui.theme.Dimensions.buttonCornerRadius

val buttonRoundedCornerShape = RoundedCornerShape(buttonCornerRadius)

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
        // TODO: Change this color
        tertiary = Primary400,
        // TODO: Add tertiary colors
        // onTertiary = DarkOnTertiary,
        // tertiaryContainer = DarkTertiaryContainer,
        // onTertiaryContainer = DarkOnTertiaryContainer,
        background = Black,
        onBackground = White,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        surface = DarkSurface,
        // surfaceVariant = DarkSurfaceVariant, // TODO: Add this color
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
        // TODO: Change this color
        tertiary = Primary400,
        // TODO: Add tertiary colors
        // onTertiary = LightOnTertiary,
        // tertiaryContainer = LightTertiaryContainer,
        // onTertiaryContainer = LightOnTertiaryContainer,
        background = White,
        onBackground = Black,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
        surface = LightSurface,
        // surfaceVariant = LightSurfaceVariant, // TODO: Add this color
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

@Composable
fun RIADigiDocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val color = colorScheme.primary.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
                    view.setBackgroundColor(color)
                    windowInsets
                }
            } else {
                window.statusBarColor = color
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(),
        content = content,
    )
}
