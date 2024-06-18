@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.util.LinkifyCompat

@Composable
fun LinkifyText(
    modifier: Modifier = Modifier,
    text: String?,
    textAlignment: Int,
    textColor: Int,
    linkTextColor: Int,
) {
    val context = LocalContext.current
    val customLinkifyTextView =
        remember {
            TextView(context)
        }
    AndroidView(
        modifier = modifier,
        factory = { customLinkifyTextView },
    ) { textView ->
        textView.text = text ?: ""
        LinkifyCompat.addLinks(textView, Linkify.WEB_URLS)

        textView.setTextColor(textColor)
        textView.setLinkTextColor(linkTextColor)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.textAlignment = textAlignment
    }
}
