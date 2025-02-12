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
                    color = OnPrimary,
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
                    color = OnPrimary,
                    textAlign = TextAlign.Center,
                    // Disable ligatures
                    fontFeatureSettings = "'liga' off, 'clig' off",
                ),
        )

    return typographyRIADigiDoc
}
