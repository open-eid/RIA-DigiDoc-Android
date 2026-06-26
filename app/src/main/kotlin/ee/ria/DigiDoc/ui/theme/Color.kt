/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
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

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.theme

import androidx.compose.ui.graphics.Color

val BlueBackground = Color(0xFF003168)

// Light
val LightPrimary = Color(0xFF003168)
val LightPrimaryContainer = Color(0xFFDDE8F3)
val LightSecondary = Color(0xFF747781)
val LightSecondaryContainer = Color(0xFFE2E2E7)
val LightError = Color(0xFFB00020)
val LightErrorContainer = Color(0xFFF6DBE0)
val LightSuccess = Color(0xFF2FB631)
val LightSuccessContainer = Color(0xFFE0F4E0)
val LightWarning = Color(0xFF7A3E00)
val LightWarningContainer = Color(0xFFFAE7C9)

val LightOnPrimary = Color.White
val LightOnPrimaryContainer = Color(0xFF0C2246)
val LightOnSecondary = Color.White
val LightOnSecondaryContainer = Color(0xFF282A2E)
val LightOnError = Color.White
val LightOnErrorContainer = Color(0xFF710015)
val LightOnSuccess = Color.White
val LightOnSuccessContainer = Color(0xFF144C15)
val LightOnWarning = Color.White
val LightOnWarningContainer = Color(0xFF5A2E00)

val LightOutline = Color(0xFF747781)
val LightOutlineVariant = Color(0xFFC3C6D1)
val LightSurface = Color(0xFFFAF9FE)
val LightSurfaceContainer = Color(0xFFEEEDF3)
val LightSurfaceContainerLowest = Color.White
val LightSurfaceContainerLow = Color(0xFFF4F3F8)
val LightSurfaceContainerHigh = Color(0xFFE8E7ED)
val LightSurfaceContainerHighest = Color(0xFFE2E2E7)
val LightSurfaceVariant = Color(0xFFE0E2ED)
val LightOnSurface = Color(0xFF1A1C1F)
val LightOnSurfaceVariant = Color(0xFF434750)
val LightInversePrimary = Color(0xFFAAC7FF)
val LightInverseSurface = Color(0xFF2F3034)
val LightInverseOnSurface = Color(0xFFF4F3F8)

// Dark
val DarkPrimary = Color(0xFFAAC7FF)
val DarkPrimaryContainer = Color(0xFF0F2C5A)
val DarkSecondary = Color(0xFFE0E2ED)
val DarkSecondaryContainer = Color(0xFF2F3034)
val DarkError = Color(0xFFFF5C79)
val DarkErrorContainer = Color(0xFF50000F)
val DarkSuccess = Color(0xFF74CE75)
val DarkSuccessContainer = Color(0xFF0C2E0C)
val DarkWarning = Color(0xFFFBAE38)
val DarkWarningContainer = Color(0xFF5A2E00)

val DarkOnPrimary = Color(0xFF0C2246)
val DarkOnPrimaryContainer = Color(0xFFB5BFCF)
val DarkOnSecondary = Color(0xFF2F3034)
val DarkOnSecondaryContainer = Color(0xFFFAF9FE)
val DarkOnError = Color(0xFF34000A)
val DarkOnErrorContainer = Color(0xFFEFCCD2)
val DarkOnSuccess = Color(0xFF144C15)
val DarkOnSuccessContainer = Color(0xFF74CE75)
val DarkOnWarning = Color(0xFF5A2E00)
val DarkOnWarningContainer = Color(0xFFF2C174)

val DarkOutline = Color(0xFF8D909B)
val DarkOutlineVariant = Color(0xFF434750)
val DarkSurface = Color(0xFF121317)
val DarkSurfaceContainer = Color(0xFF1E2023)
val DarkSurfaceContainerLowest = Color(0xFF0D0E12)
val DarkSurfaceContainerLow = Color(0xFF1A1C1F)
val DarkSurfaceContainerHigh = Color(0xFF282A2E)
val DarkSurfaceContainerHighest = Color(0xFF333539)
val DarkSurfaceVariant = Color(0xFF434750)
val DarkOnSurface = Color(0xFFE2E2E7)
val DarkOnSurfaceVariant = Color(0xFFC3C6D1)
val DarkInversePrimary = Color(0xFF003168)
val DarkInverseSurface = Color(0xFFE2E2E7)
val DarkInverseOnSurface = Color(0xFF2F3034)

// General
val Transparent = Color(0x00FFFFFF)

data class ExtendedColorScheme(
    val success: Color,
    val successContainer: Color,
    val onSuccess: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarning: Color,
    val onWarningContainer: Color,
)
