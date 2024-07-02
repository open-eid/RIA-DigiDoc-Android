@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withStyle
import ee.ria.DigiDoc.ui.theme.Blue500

@Composable
fun DynamicText(
    modifier: Modifier,
    text: String,
    textStyle: TextStyle =
        TextStyle(
            textAlign = TextAlign.Start,
        ),
) {
    val uriHandler = LocalUriHandler.current
    val annotatedStringWithLinks = createAnnotatedStringWithLinks(text)

    ClickableText(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .let {
                    val urlInText =
                        annotatedStringWithLinks
                            .getStringAnnotations(tag = "URL", start = 0, end = annotatedStringWithLinks.length)
                            .firstOrNull()
                            ?.item
                    if (!urlInText.isNullOrEmpty()) {
                        it.clickable(enabled = true, onClick = { uriHandler.openUri(urlInText) })
                    } else {
                        it
                    }
                },
        text = annotatedStringWithLinks,
        style = textStyle,
        onClick = { offset ->
            annotatedStringWithLinks
                .getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                }
        },
    )
}

fun createAnnotatedStringWithLinks(text: String): AnnotatedString {
    val words = text.split(" ")

    return buildAnnotatedString {
        words.forEach { word ->
            if (Patterns.WEB_URL.matcher(word).matches()) {
                pushStringAnnotation(tag = "URL", annotation = word)
                withStyle(
                    style =
                        SpanStyle(
                            color = Blue500,
                            textDecoration = Underline,
                        ),
                ) {
                    append(word)
                }
                pop()
            } else {
                append(word)
            }
            append(" ")
        }
    }
}
