/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.theme

import android.app.Activity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
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
    val accessibilityFocusColor =
        if (isSystemInDarkTheme()) {
            LightColorScheme.primary
        } else {
            DarkColorScheme.primary
        }

    val accessibilityFocusAlpha =
        RippleAlpha(
            pressedAlpha = 0.1f,
            focusedAlpha = 0.4f,
            draggedAlpha = 0.16f,
            hoveredAlpha = 0.08f,
        )

    val useDarkTheme = darkTheme ?: isSystemInDarkTheme()
    val colorScheme =
        when {
            dynamicColor -> {
                val context = LocalContext.current
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            useDarkTheme -> DarkColorScheme
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
        content = {
            AccessibilityFocusProvider(
                focusColor = accessibilityFocusColor,
                alpha = accessibilityFocusAlpha,
                content = content,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessibilityFocusProvider(
    focusColor: Color,
    alpha: RippleAlpha,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalIndication provides
            remember(focusColor) {
                FocusIndication(
                    color = focusColor,
                    alpha = alpha.focusedAlpha,
                )
            },
        LocalRippleConfiguration provides
            RippleConfiguration(
                color = focusColor,
                rippleAlpha = alpha,
            ),
        content = content,
    )
}
