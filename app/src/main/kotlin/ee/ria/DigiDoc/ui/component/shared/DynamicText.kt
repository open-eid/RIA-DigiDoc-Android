@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import ee.ria.DigiDoc.ui.theme.Dimensions.LINE_HEIGHT

@Composable
fun DynamicText(
    modifier: Modifier,
    text: String,
    textStyle: TextStyle =
        TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            textAlign = TextAlign.Start,
            lineHeight = TextUnit(LINE_HEIGHT, TextUnitType.Sp),
        ),
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary
    val annotatedStringWithLinks = createAnnotatedStringWithLinks(text, linkColor)

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
        Modifier.pointerInput(onClick) {
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
    text: String,
    linkColor: Color,
): AnnotatedString {
    val words = text.split(" ")

    return buildAnnotatedString {
        words.forEach { word ->
            if (Patterns.WEB_URL.matcher(word).matches()) {
                pushStringAnnotation(tag = "URL", annotation = word)
                withStyle(
                    style =
                        SpanStyle(
                            color = linkColor,
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
