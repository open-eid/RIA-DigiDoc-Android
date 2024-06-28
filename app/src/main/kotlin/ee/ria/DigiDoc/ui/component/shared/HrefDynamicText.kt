@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import ee.ria.DigiDoc.ui.theme.Blue500

@Composable
fun HrefDynamicText(
    modifier: Modifier,
    text1: String?,
    text2: String?,
    linkText: String,
    linkUrl: String,
    textStyle: TextStyle =
        TextStyle(
            textAlign = TextAlign.Start,
        ),
) {
    val uriHandler = LocalUriHandler.current
    val annotatedStringWithLinks = createAnnotatedStringWithLinks(text1, text2, linkText, linkUrl)

    ClickableText(
        modifier = modifier.fillMaxWidth(),
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

fun createAnnotatedStringWithLinks(
    text1: String?,
    text2: String?,
    linkText: String,
    linkUrl: String,
): AnnotatedString {
    return buildAnnotatedString {
        val mStr = "$text1\n$linkText.\n$text2"
        val mStartIndex = mStr.indexOf(linkText)
        val mEndIndex = mStartIndex + linkText.length

        append(mStr)
        addStyle(
            style =
                SpanStyle(
                    color = Blue500,
                    textDecoration = Underline,
                ),
            start = mStartIndex,
            end = mEndIndex,
        )
        addStringAnnotation(
            tag = "URL",
            annotation = linkUrl,
            start = mStartIndex,
            end = mEndIndex,
        )
    }
}
