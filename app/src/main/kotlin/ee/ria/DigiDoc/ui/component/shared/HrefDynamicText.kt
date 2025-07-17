@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
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

    val onClick: (Int) -> Unit = { offset ->
        annotatedStringWithLinks
            .getStringAnnotations(tag = "URL", start = offset, end = offset)
            .firstOrNull()
            ?.let { annotation ->
                uriHandler.openUri(annotation.item)
            }
    }

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator =
        modifier.pointerInput(onClick) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    onClick(layoutResult.getOffsetForPosition(pos))
                }
            }
        }

    BasicText(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
                .testTag("hrefDynamicText")
                .then(pressIndicator)
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
    )
}

fun createAnnotatedStringWithLinks(
    text1: String?,
    text2: String?,
    linkText: String,
    linkUrl: String,
    linkColor: Color,
    showLinkOnOneLine: Boolean,
): AnnotatedString =
    buildAnnotatedString {
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
