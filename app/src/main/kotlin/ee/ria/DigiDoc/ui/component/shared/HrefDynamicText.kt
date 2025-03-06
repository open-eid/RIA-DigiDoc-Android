@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withStyle

@Composable
fun HrefDynamicText(
    modifier: Modifier,
    text1: String?,
    text2: String?,
    linkText: String,
    linkUrl: String,
    showLinkOnOneLine: Boolean,
    textStyle: TextStyle =
        TextStyle(
            textAlign = TextAlign.Start,
        ),
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.onSecondaryContainer
    val annotatedStringWithLinks =
        createAnnotatedStringWithLinks(text1, text2, linkText, linkUrl, linkColor, showLinkOnOneLine)

    val mStr = "$text1\n$linkText.\n$text2"
    val mStartIndex = mStr.indexOf(linkText)
    val mEndIndex = mStartIndex + linkText.length

    ClickableText(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("hrefDynamicText"),
        text = annotatedStringWithLinks,
        style = textStyle,
        onClick = {
            annotatedStringWithLinks
                .getStringAnnotations("URL", mStartIndex, mEndIndex)
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        },
    )
}

fun createAnnotatedStringWithLinks(
    text1: String?,
    text2: String?,
    linkText: String,
    linkUrl: String,
    linkColor: Color,
    showLinkOnOneLine: Boolean,
): AnnotatedString {
    return buildAnnotatedString {
        if (showLinkOnOneLine) {
            append("$text1 ")
        } else {
            append("$text1\n")
        }

        pushStringAnnotation(tag = "URL", annotation = linkUrl)
        withStyle(
            style =
                SpanStyle(
                    color = linkColor,
                    textDecoration = Underline,
                ),
        ) {
            append(linkText)
        }
        pop()

        append("\n$text2")
    }
}
