@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.text.TextUtils
import android.view.View
import android.view.View.NOT_FOCUSABLE
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MiddleEllipsizeMultilineText(
    modifier: Modifier = Modifier,
    text: String?,
    textColor: Int,
    maxLines: Int = 1,
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
                                textView.paint, (textView.width * (maxLines / 1.5)).toFloat(),
                                TextUtils.TruncateAt.MIDDLE,
                            )
                    }
                }
            },
        )

        textView.setTextColor(textColor)
    }
}
