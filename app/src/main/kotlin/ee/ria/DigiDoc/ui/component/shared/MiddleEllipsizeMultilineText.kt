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

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.View.NOT_FOCUSABLE
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MiddleEllipsizeMultilineText(
    modifier: Modifier = Modifier,
    text: String?,
    textColor: Int,
    maxLines: Int = 1,
    textStyle: TextStyle = TextStyle(),
) {
    val context = LocalContext.current
    val middleEllipsizeMultilineTextView =
        remember {
            TextView(context).apply {
                isFocusableInTouchMode = false
                isFocusable = false
                isClickable = false
            }
        }
    AndroidView(
        modifier = modifier,
        factory = { middleEllipsizeMultilineTextView },
    ) { textView ->
        textView.text = text ?: ""
        textView.maxLines = maxLines
        textView.focusable = NOT_FOCUSABLE
        textView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        textView.isFocusableInTouchMode = false
        textView.isFocusable = false
        textView.isClickable = false
        textView.getViewTreeObserver().addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    textView.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    val lineCount: Int = textView.lineCount

                    if (lineCount > maxLines) {
                        textView.text =
                            TextUtils.ellipsize(
                                textView.text,
                                textView.paint,
                                (textView.width * (maxLines / 1.5)).toFloat(),
                                TextUtils.TruncateAt.MIDDLE,
                            )
                    }
                }
            },
        )

        textView.setTextColor(textColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textStyle.fontSize.value)
        textView.setTypeface(
            null,
            if (textStyle.fontWeight == FontWeight.Bold) {
                Typeface.BOLD
            } else {
                Typeface.NORMAL
            },
        )
    }
}
