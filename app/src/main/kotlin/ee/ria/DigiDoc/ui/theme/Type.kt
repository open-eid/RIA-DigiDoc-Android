@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.theme

import android.content.Context
import android.os.Build
import android.util.TypedValue
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
fun getTypography(context: Context): Typography {
    return Typography(
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = pixelsToSp(context, 14f),
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = pixelsToSp(context, 16f),
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
     */
    )
}

fun pixelsToSp(
    context: Context,
    value: Float,
): TextUnit {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        return TypedValue.convertDimensionToPixels(
            TypedValue.COMPLEX_UNIT_PX,
            value,
            context.resources.displayMetrics,
        ).sp
    }
    val scaledDensity = context.resources.displayMetrics.scaledDensity
    return (value / scaledDensity).sp
}
