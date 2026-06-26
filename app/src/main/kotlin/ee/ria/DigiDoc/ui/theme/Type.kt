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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ee.ria.DigiDoc.R

// Set of Material typography styles to start with
fun getTypography(): Typography {
    val fontRobotoCondensed =
        FontFamily(
            Font(R.font.roboto_condensed),
        )

    val typographyRIADigiDoc =
        Typography(
            displayLarge =
                TextStyle(
                    fontFamily = fontRobotoCondensed,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Normal,
                    textAlign = TextAlign.Center,
                    // Disable ligatures
                    fontFeatureSettings = "'liga' off, 'clig' off",
                ),
            displaySmall =
                TextStyle(
                    fontFamily = fontRobotoCondensed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    textAlign = TextAlign.Center,
                    // Disable ligatures
                    fontFeatureSettings = "'liga' off, 'clig' off",
                ),
        )

    return typographyRIADigiDoc
}
